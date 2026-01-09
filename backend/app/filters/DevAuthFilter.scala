package filters

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

/**
 * Development Authentication Filter
 * 
 * This filter provides a mock authentication mechanism for local development,
 * bypassing Shibboleth/HAKA authentication. It should NEVER be enabled in production.
 * 
 * To enable: Set DEV_AUTH_ENABLED=true in environment variables.
 * The filter will inject a mock user identity into the request headers.
 */
class DevAuthFilter @Inject()(
  configuration: Configuration,
  implicit val ec: ExecutionContext
) extends EssentialFilter {

  private val logger = Logger(getClass)
  
  private val isDevAuthEnabled: Boolean = 
    configuration.getOptional[Boolean]("exam.dev.auth.enabled").getOrElse(false)
  
  private val devAuthUser: String = 
    configuration.getOptional[String]("exam.dev.auth.user").getOrElse("dev-user")

  override def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
    if (isDevAuthEnabled) {
      // WARNING: This should only be enabled in development
      if (configuration.getOptional[String]("play.mode").contains("prod")) {
        logger.error("SECURITY WARNING: Dev auth is enabled in production mode! This is a security risk.")
      } else {
        logger.warn("Dev auth mode is ENABLED. This bypasses production authentication.")
      }
      
      // Inject mock user identity into request headers
      // In production, these would come from Shibboleth/HAKA
      val newHeaders = request.headers.add(
        "X-User-Id" -> devAuthUser,
        "X-User-Email" -> s"$devAuthUser@dev.local",
        "X-User-Name" -> s"Development User ($devAuthUser)",
        "X-Auth-Source" -> "dev-bypass"
      )
      
      next(request.withHeaders(newHeaders))
    } else {
      // Dev auth disabled - pass through (production auth will handle it)
      next(request)
    }
  }
}
