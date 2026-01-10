package filters

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._
import java.time.Instant

/**
 * Rate Limiting Filter
 *
 * Implements token bucket algorithm for rate limiting API endpoints.
 * Limits requests per IP address to prevent abuse and DoS attacks.
 *
 * Configuration:
 * - exam.rate.limit.enabled: Enable/disable rate limiting (default: true)
 * - exam.rate.limit.requests: Number of requests allowed (default: 100)
 * - exam.rate.limit.window: Time window in seconds (default: 60)
 * - exam.rate.limit.api.paths: Comma-separated list of paths to rate limit
 */
class RateLimitFilter @Inject()(
  configuration: Configuration,
  implicit val ec: ExecutionContext
) extends EssentialFilter {

  private val logger = Logger(getClass)

  private val isEnabled: Boolean = 
    configuration.getOptional[Boolean]("exam.rate.limit.enabled").getOrElse(true)
  
  private val maxRequests: Int = 
    configuration.getOptional[Int]("exam.rate.limit.requests").getOrElse(100)
  
  private val windowSeconds: Int = 
    configuration.getOptional[Int]("exam.rate.limit.window").getOrElse(60)
  
  private val apiPaths: Set[String] = 
    configuration.getOptional[String]("exam.rate.limit.api.paths")
      .map(_.split(",").map(_.trim).toSet)
      .getOrElse(Set("/api/v1/"))

  // In-memory rate limit store: IP -> (count, windowStart)
  private val rateLimitStore = new ConcurrentHashMap[String, (Int, Long)]().asScala

  // Cleanup old entries periodically (every 5 minutes)
  private val cleanupInterval = 5 * 60 * 1000L // 5 minutes
  private var lastCleanup = System.currentTimeMillis()

  override def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
    if (!isEnabled) {
      next(request)
    } else if (shouldRateLimit(request.path)) {
      val clientIp = getClientIp(request)
      val currentTime = System.currentTimeMillis()
      val windowStart = (currentTime / (windowSeconds * 1000L)) * (windowSeconds * 1000L)

      // Cleanup old entries periodically
      if (currentTime - lastCleanup > cleanupInterval) {
        cleanupOldEntries(currentTime)
        lastCleanup = currentTime
      }

      rateLimitStore.get(clientIp) match {
        case Some((count, storedWindowStart)) if storedWindowStart == windowStart =>
          if (count >= maxRequests) {
            logger.warn(s"Rate limit exceeded for IP: $clientIp, path: ${request.path}")
            Future.successful(
              Results.TooManyRequests(
                play.api.libs.json.Json.obj(
                  "error" -> "RATE_LIMIT_EXCEEDED",
                  "message" -> s"Rate limit exceeded. Maximum $maxRequests requests per $windowSeconds seconds.",
                  "retryAfter" -> (windowStart + (windowSeconds * 1000L) - currentTime) / 1000
                )
              ).withHeaders(
                "X-RateLimit-Limit" -> maxRequests.toString,
                "X-RateLimit-Remaining" -> "0",
                "X-RateLimit-Reset" -> (windowStart + (windowSeconds * 1000L)).toString,
                "Retry-After" -> ((windowStart + (windowSeconds * 1000L) - currentTime) / 1000).toString
              )
            )
          } else {
            rateLimitStore.update(clientIp, (count + 1, windowStart))
            next(request).map(_.withHeaders(
              "X-RateLimit-Limit" -> maxRequests.toString,
              "X-RateLimit-Remaining" -> (maxRequests - count - 1).toString,
              "X-RateLimit-Reset" -> (windowStart + (windowSeconds * 1000L)).toString
            ))
          }
        case _ =>
          // New window or new IP
          rateLimitStore.update(clientIp, (1, windowStart))
          next(request).map(_.withHeaders(
            "X-RateLimit-Limit" -> maxRequests.toString,
            "X-RateLimit-Remaining" -> (maxRequests - 1).toString,
            "X-RateLimit-Reset" -> (windowStart + (windowSeconds * 1000L)).toString
          ))
      }
    } else {
      next(request)
    }
  }

  private def shouldRateLimit(path: String): Boolean = {
    apiPaths.exists(apiPath => path.startsWith(apiPath))
  }

  private def getClientIp(request: RequestHeader): String = {
    // Check X-Forwarded-For header (for reverse proxies)
    request.headers.get("X-Forwarded-For")
      .flatMap(_.split(",").headOption.map(_.trim))
      .getOrElse(
        // Fall back to X-Real-IP (nginx)
        request.headers.get("X-Real-IP")
          .getOrElse(request.remoteAddress)
      )
  }

  private def cleanupOldEntries(currentTime: Long): Unit = {
    val cutoffTime = currentTime - (windowSeconds * 1000L * 2) // Keep entries from last 2 windows
    val keysToRemove = rateLimitStore.collect {
      case (ip, (_, windowStart)) if windowStart < cutoffTime => ip
    }.toSeq
    
    keysToRemove.foreach(rateLimitStore.remove)
    if (keysToRemove.nonEmpty) {
      logger.debug(s"Cleaned up ${keysToRemove.size} old rate limit entries")
    }
  }
}
