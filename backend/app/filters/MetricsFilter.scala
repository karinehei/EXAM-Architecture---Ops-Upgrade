package filters

import javax.inject.Inject
import play.api.mvc._
import play.api.Logger
import controllers.MetricsController

import scala.concurrent.{ExecutionContext, Future}

/**
 * Metrics Filter
 * 
 * Tracks request metrics: count, latency, and error rates.
 * Records metrics for Prometheus-style metrics endpoint.
 */
class MetricsFilter @Inject()(
  metricsController: MetricsController,
  implicit val ec: ExecutionContext
) extends EssentialFilter {

  private val logger = Logger(getClass)

  override def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
    val startTime = System.currentTimeMillis()
    val endpoint = s"${request.method} ${request.path}"

    // Skip metrics endpoint itself
    if (request.path == "/metrics") {
      next(request)
    } else {
      next(request).map { result =>
        val duration = System.currentTimeMillis() - startTime
        val isError = result.header.status >= 400
        
        // Record metrics asynchronously
        Future {
          metricsController.recordRequest(endpoint, duration, isError)
        }
        
        result
      }
    }
  }
}
