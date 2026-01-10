package filters

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

/**
 * Security Headers Filter
 * 
 * Adds security-related HTTP headers to all responses to protect against
 * common web vulnerabilities.
 * 
 * Headers added:
 * - Strict-Transport-Security (HSTS): Forces HTTPS
 * - X-Frame-Options: Prevents clickjacking
 * - X-Content-Type-Options: Prevents MIME type sniffing
 * - Content-Security-Policy: Restricts resource loading
 * - X-XSS-Protection: Enables XSS filter (legacy)
 * - Referrer-Policy: Controls referrer information
 * - Permissions-Policy: Restricts browser features
 * 
 * Configuration:
 * - exam.security.headers.enabled: Enable/disable security headers (default: true)
 * - exam.security.headers.hsts.maxAge: HSTS max age in seconds (default: 31536000 = 1 year)
 * - exam.security.headers.csp: Custom CSP policy (optional)
 */
class SecurityHeadersFilter @Inject()(
  configuration: Configuration,
  implicit val ec: ExecutionContext
) extends EssentialFilter {

  private val logger = Logger(getClass)

  private val isEnabled: Boolean = 
    configuration.getOptional[Boolean]("exam.security.headers.enabled").getOrElse(true)
  
  private val hstsMaxAge: Int = 
    configuration.getOptional[Int]("exam.security.headers.hsts.maxAge").getOrElse(31536000) // 1 year
  
  private val customCsp: Option[String] = 
    configuration.getOptional[String]("exam.security.headers.csp")

  // Default Content Security Policy
  // Allows same-origin resources, inline scripts/styles (for Angular), and API calls
  // Also allows unpkg.com for Swagger UI resources
  private val defaultCsp = 
    "default-src 'self'; " +
    "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com; " + // unpkg.com for Swagger UI
    "style-src 'self' 'unsafe-inline' https://unpkg.com; " + // unpkg.com for Swagger UI
    "img-src 'self' data: https:; " +
    "font-src 'self' data:; " +
    "connect-src 'self' https:; " +
    "frame-ancestors 'none'; " +
    "base-uri 'self'; " +
    "form-action 'self'"

  private val cspPolicy = customCsp.getOrElse(defaultCsp)

  override def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
    if (!isEnabled) {
      next(request)
    } else {
      next(request).map { result =>
        val baseHeaders = Seq(
          // X-Frame-Options: Prevent clickjacking
          "X-Frame-Options" -> "DENY",
          // X-Content-Type-Options: Prevent MIME sniffing
          "X-Content-Type-Options" -> "nosniff",
          // Content-Security-Policy: Restrict resource loading
          "Content-Security-Policy" -> cspPolicy,
          // X-XSS-Protection: Enable XSS filter (legacy, but still useful)
          "X-XSS-Protection" -> "1; mode=block",
          // Referrer-Policy: Control referrer information
          "Referrer-Policy" -> "strict-origin-when-cross-origin",
          // Permissions-Policy: Restrict browser features
          "Permissions-Policy" -> "geolocation=(), microphone=(), camera=()"
        )
        
        // Add HSTS only if request is HTTPS
        val headers = if (request.secure || request.headers.get("X-Forwarded-Proto").contains("https")) {
          baseHeaders :+ ("Strict-Transport-Security" -> s"max-age=$hstsMaxAge; includeSubDomains; preload")
        } else {
          baseHeaders
        }
        
        result.withHeaders(headers: _*)
      }
    }
  }
}
