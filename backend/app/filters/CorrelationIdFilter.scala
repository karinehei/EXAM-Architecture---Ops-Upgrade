package filters

import javax.inject.Inject
import play.api.mvc._
import play.api.Logger
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

/**
 * Correlation ID Filter
 * 
 * Generates or reads X-Request-ID header for request tracing.
 * The correlation ID is included in all log entries for this request.
 */
class CorrelationIdFilter @Inject()(
  implicit ec: ExecutionContext
) extends EssentialFilter {

  private val logger = Logger(getClass)
  private val CorrelationIdHeader = "X-Request-ID"
  private val CorrelationIdMDCKey = "correlationId"

  override def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
    // Get or generate correlation ID
    val correlationId = request.headers.get(CorrelationIdHeader)
      .getOrElse(UUID.randomUUID().toString)

    // Add to MDC for logging
    MDC.put(CorrelationIdMDCKey, correlationId)

    // Process request
    val result = next(request.withHeaders(
      request.headers.add(CorrelationIdHeader -> correlationId)
    ))

    // Add correlation ID to response headers and clean up MDC
    result.map { response =>
      MDC.remove(CorrelationIdMDCKey)
      response.withHeaders(CorrelationIdHeader -> correlationId)
    }
  }
}
