package controllers

import javax.inject._
import play.api.mvc._
import play.api.Logger

import scala.collection.mutable

/**
 * Metrics Controller
 * 
 * Provides Prometheus-style metrics endpoint.
 * Tracks request counts, latency, and error rates.
 */
@Singleton
class MetricsController @Inject()(
  val controllerComponents: ControllerComponents
) extends BaseController {

  private val logger = Logger(getClass)
  
  // Simple in-memory metrics store (for production, consider using a proper metrics library)
  // Using synchronized collections for thread safety
  private val requestCounts = mutable.Map[String, Long]().withDefaultValue(0L)
  private val errorCounts = mutable.Map[String, Long]().withDefaultValue(0L)
  private val latencySum = mutable.Map[String, Long]().withDefaultValue(0L)
  private val latencyCount = mutable.Map[String, Long]().withDefaultValue(0L)
  private val latencyHistogram = mutable.Map[String, mutable.ListBuffer[Long]]().withDefaultValue(mutable.ListBuffer.empty[Long])
  
  // Synchronization object for thread-safe access
  private val metricsLock = new Object()

  def metrics = Action { implicit request =>
    val timestamp = System.currentTimeMillis() / 1000 // Unix timestamp
    
    val metrics = new StringBuilder
    
    // Thread-safe snapshot of metrics
    val (snapshotCounts, snapshotErrors, snapshotSum, snapshotCount, snapshotHistogram) = metricsLock.synchronized {
      (
        requestCounts.toMap,
        errorCounts.toMap,
        latencySum.toMap,
        latencyCount.toMap,
        latencyHistogram.map { case (k, v) => (k, v.toSeq) }.toMap
      )
    }
    
    // Request counts
    snapshotCounts.foreach { case (endpoint, count) =>
      metrics.append(s"http_requests_total{endpoint=\"$endpoint\"} $count $timestamp\n")
    }
    
    // Error counts
    snapshotErrors.foreach { case (endpoint, count) =>
      metrics.append(s"http_errors_total{endpoint=\"$endpoint\"} $count $timestamp\n")
    }
    
    // Average latency
    snapshotSum.foreach { case (endpoint, sum) =>
      val count = snapshotCount.getOrElse(endpoint, 1L)
      val avg = if (count > 0) sum / count else 0
      metrics.append(s"http_request_duration_seconds_avg{endpoint=\"$endpoint\"} ${avg / 1000.0} $timestamp\n")
    }
    
    // Latency percentiles (p50, p95, p99)
    snapshotHistogram.foreach { case (endpoint, latencies) =>
      if (latencies.nonEmpty) {
        val sorted = latencies.sorted
        val p50 = percentile(sorted, 0.50)
        val p95 = percentile(sorted, 0.95)
        val p99 = percentile(sorted, 0.99)
        
        metrics.append(s"http_request_duration_seconds{endpoint=\"$endpoint\",quantile=\"0.5\"} ${p50 / 1000.0} $timestamp\n")
        metrics.append(s"http_request_duration_seconds{endpoint=\"$endpoint\",quantile=\"0.95\"} ${p95 / 1000.0} $timestamp\n")
        metrics.append(s"http_request_duration_seconds{endpoint=\"$endpoint\",quantile=\"0.99\"} ${p99 / 1000.0} $timestamp\n")
      }
    }
    
    // Keep only last 1000 measurements per endpoint to avoid memory issues
    metricsLock.synchronized {
      latencyHistogram.foreach { case (endpoint, latencies) =>
        if (latencies.length > 1000) {
          latencyHistogram(endpoint) = latencies.takeRight(1000)
        }
      }
    }
    
    Ok(metrics.toString()).as("text/plain; version=0.0.4")
  }

  // Helper method to record metrics (called by MetricsFilter)
  def recordRequest(endpoint: String, duration: Long, isError: Boolean): Unit = {
    metricsLock.synchronized {
      requestCounts(endpoint) += 1
      if (isError) {
        errorCounts(endpoint) += 1
      }
      latencySum(endpoint) += duration
      latencyCount(endpoint) += 1
      latencyHistogram(endpoint) += duration
    }
  }

  private def percentile(sorted: Seq[Long], p: Double): Long = {
    if (sorted.isEmpty) 0L
    else {
      val index = Math.ceil(sorted.length * p).toInt - 1
      sorted(Math.max(0, Math.min(index, sorted.length - 1)))
    }
  }
}
