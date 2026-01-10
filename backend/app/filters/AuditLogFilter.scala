package filters

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Audit Logging Filter
 * 
 * Logs all API access for security auditing and compliance.
 * Records: endpoint, HTTP method, user identity, timestamp, IP address, status code.
 * 
 * Configuration:
 * - exam.audit.enabled: Enable/disable audit logging (default: true)
 * - exam.audit.log.api.only: Only log API endpoints (default: true)
 * - exam.audit.log.success: Log successful requests (default: true)
 * - exam.audit.log.failures: Log failed requests (default: true)
 */
class AuditLogFilter @Inject()(
  configuration: Configuration,
  implicit val ec: ExecutionContext
) extends EssentialFilter {

  private val logger = Logger("audit")

  private val isEnabled: Boolean = 
    configuration.getOptional[Boolean]("exam.audit.enabled").getOrElse(true)
  
  private val apiOnly: Boolean = 
    configuration.getOptional[Boolean]("exam.audit.log.api.only").getOrElse(true)
  
  private val logSuccess: Boolean = 
    configuration.getOptional[Boolean]("exam.audit.log.success").getOrElse(true)
  
  private val logFailures: Boolean = 
    configuration.getOptional[Boolean]("exam.audit.log.failures").getOrElse(true)

  private val timestampFormatter = DateTimeFormatter.ISO_INSTANT

  override def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
    if (!isEnabled) {
      next(request)
    } else if (shouldLog(request.path)) {
      val startTime = System.currentTimeMillis()
      val userId = request.headers.get("X-User-Id").getOrElse("anonymous")
      val userEmail = request.headers.get("X-User-Email").getOrElse("")
      val clientIp = getClientIp(request)
      val correlationId = request.headers.get("X-Request-ID").getOrElse("")

      next(request).map { result =>
        val duration = System.currentTimeMillis() - startTime
        val timestamp = Instant.now()
        val statusCode = result.header.status

        val shouldLogThis = (logSuccess && statusCode < 400) || (logFailures && statusCode >= 400)

        if (shouldLogThis) {
          val auditEntry = Map(
            "timestamp" -> timestampFormatter.format(timestamp),
            "method" -> request.method,
            "path" -> request.path,
            "queryString" -> request.rawQueryString,
            "statusCode" -> statusCode.toString,
            "userId" -> userId,
            "userEmail" -> userEmail,
            "clientIp" -> clientIp,
            "userAgent" -> request.headers.get("User-Agent").getOrElse(""),
            "correlationId" -> correlationId,
            "durationMs" -> duration.toString
          )

          // Log as structured JSON-like format for easy parsing
          val logMessage = auditEntry.map { case (k, v) => s"$k=$v" }.mkString(" ")
          
          if (statusCode >= 400) {
            logger.warn(s"AUDIT: $logMessage")
          } else {
            logger.info(s"AUDIT: $logMessage")
          }
        }

        result
      }
    } else {
      next(request)
    }
  }

  private def shouldLog(path: String): Boolean = {
    if (!apiOnly) {
      true // Log all requests
    } else {
      // Only log API endpoints
      path.startsWith("/api/") || path.startsWith("/health") || path.startsWith("/metrics")
    }
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
}
