package app

import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.slick.SlickComponents
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.DbName
import filters.{DevAuthFilter, CorrelationIdFilter, MetricsFilter}
import controllers.MetricsController

class ExamApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new ExamComponents(context).application
  }
}

class ExamComponents(context: Context) extends BuiltInComponentsFromContext(context) with SlickComponents {
  lazy val router = new _root_.router.Routes(httpErrorHandler, homeController, healthController, metricsController, reportController)
  
  lazy val homeController = new _root_.controllers.HomeController(controllerComponents)
  lazy val healthController = new _root_.controllers.HealthController(controllerComponents, dbConfigProvider)
  lazy val reportRepository = new _root_.repositories.ReportRepository(dbConfigProvider)(executionContext)
  lazy val reportController = new _root_.controllers.ReportController(controllerComponents, reportRepository)(executionContext)
  lazy val metricsController = new MetricsController(controllerComponents)
  
  // Filters
  lazy val correlationIdFilter = new CorrelationIdFilter()(executionContext)
  lazy val devAuthFilter = new DevAuthFilter(configuration, executionContext)
  lazy val metricsFilter = new MetricsFilter(metricsController, executionContext)
  
  // Register filters (order matters: correlation ID first, then auth, then metrics)
  override lazy val httpFilters = Seq(correlationIdFilter, devAuthFilter, metricsFilter)
  
  // Database configuration (provided by SlickComponents trait)
  // Create DatabaseConfigProvider using slickApi from SlickComponents
  import slick.basic.BasicProfile
  lazy val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    def get[P <: BasicProfile] = slickApi.dbConfig[P](DbName("default"))
  }
}
