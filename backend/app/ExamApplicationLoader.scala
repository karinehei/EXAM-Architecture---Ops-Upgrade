package app

import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.slick.SlickComponents
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.DbName
import filters.DevAuthFilter

class ExamApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new ExamComponents(context).application
  }
}

class ExamComponents(context: Context) extends BuiltInComponentsFromContext(context) with SlickComponents {
  lazy val router = new _root_.router.Routes(httpErrorHandler, homeController, healthController, reportController)
  
  lazy val homeController = new _root_.controllers.HomeController(controllerComponents)
  lazy val healthController = new _root_.controllers.HealthController(controllerComponents, dbConfigProvider)
  lazy val reportController = new _root_.controllers.ReportController(controllerComponents, dbConfigProvider)
  
  // Development authentication filter (only active when DEV_AUTH_ENABLED=true)
  lazy val devAuthFilter = new DevAuthFilter(configuration, executionContext)
  
  // Register filters
  override lazy val httpFilters = Seq(devAuthFilter)
  
  // Database configuration (provided by SlickComponents trait)
  // Create DatabaseConfigProvider using slickApi from SlickComponents
  import slick.basic.BasicProfile
  lazy val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    def get[P <: BasicProfile] = slickApi.dbConfig[P](DbName("default"))
  }
}
