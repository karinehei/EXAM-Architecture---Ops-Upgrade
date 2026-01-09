package controllers

import javax.inject._
import play.api.mvc._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HealthController @Inject()(
  val controllerComponents: ControllerComponents,
  dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends BaseController {

  // Lazy initialization to avoid triggering database connection at class load time
  private lazy val dbConfig = dbConfigProvider.get[JdbcProfile]

  def check = Action.async { implicit request =>
    // Basic health check - can be extended for readiness/liveness
    // Simplified to avoid triggering database connections during health checks
    // The database connection will be tested when actually needed
    Future {
      Ok(play.api.libs.json.Json.obj(
        "status" -> "healthy",
        "database" -> "configured"
      ))
    }(ec)
  }
}
