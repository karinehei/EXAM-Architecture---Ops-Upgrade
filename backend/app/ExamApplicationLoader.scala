package app

import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.slick.SlickComponents
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.DbName
import filters.{DevAuthFilter, CorrelationIdFilter, MetricsFilter, RateLimitFilter, SecurityHeadersFilter, AuditLogFilter}

class ExamApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    // Skip logback configuration in test mode to avoid version conflicts
    // Tests will use Play's default logging configuration
    val isTestMode = context.environment.mode == Mode.Test
    if (!isTestMode) {
      LoggerConfigurator(context.environment.classLoader).foreach {
        _.configure(context.environment, context.initialConfiguration, Map.empty)
      }
    }
    new ExamComponents(context).application
  }
}

class ExamComponents(context: Context) extends BuiltInComponentsFromContext(context) with SlickComponents {
  lazy val router = new _root_.router.Routes(httpErrorHandler, homeController, healthController, metricsController, swaggerController, reportController)
  
  lazy val homeController = new _root_.controllers.HomeController(controllerComponents)
  lazy val healthController = new _root_.controllers.HealthController(controllerComponents, dbConfigProvider)
  lazy val swaggerController = new _root_.controllers.SwaggerController(controllerComponents, environment)
  lazy val reportRepository = new _root_.repositories.ReportRepository(dbConfigProvider)(executionContext)
  lazy val reportController = new _root_.controllers.ReportController(controllerComponents, reportRepository)(executionContext)
  lazy val metricsController = new _root_.controllers.MetricsController(controllerComponents)
  
  // Filters (order matters!)
  // 1. Correlation ID - must be first to add ID to all requests
  lazy val correlationIdFilter = new CorrelationIdFilter()(executionContext)
  // 2. Security headers - add early so they're on all responses
  lazy val securityHeadersFilter = new SecurityHeadersFilter(configuration, executionContext)
  // 3. Rate limiting - check before processing request
  lazy val rateLimitFilter = new RateLimitFilter(configuration, executionContext)
  // 4. Audit logging - log all API access
  lazy val auditLogFilter = new AuditLogFilter(configuration, executionContext)
  // 5. Dev auth - authentication (dev mode only)
  lazy val devAuthFilter = new DevAuthFilter(configuration, executionContext)
  // 6. Metrics - track performance (last, after all processing)
  lazy val metricsFilter = new MetricsFilter(metricsController, executionContext)
  
  // Register filters in order
  override lazy val httpFilters = Seq(
    correlationIdFilter,
    securityHeadersFilter,
    rateLimitFilter,
    auditLogFilter,
    devAuthFilter,
    metricsFilter
  )
  
  // Database configuration (provided by SlickComponents trait)
  // Create DatabaseConfigProvider using slickApi from SlickComponents
  import slick.basic.BasicProfile
  lazy val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    def get[P <: BasicProfile] = slickApi.dbConfig[P](DbName("default"))
  }
}
