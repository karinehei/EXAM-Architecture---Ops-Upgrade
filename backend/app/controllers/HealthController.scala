package controllers

import javax.inject._
import play.api.mvc._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HealthController @Inject()(
  val controllerComponents: ControllerComponents,
  dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends BaseController {

  // Lazy initialization to avoid triggering database connection at class load time
  private lazy val dbConfig = dbConfigProvider.get[JdbcProfile]

  /**
   * Liveness probe - indicates if the application is running
   * Should return 200 if the process is alive, regardless of dependencies
   */
  def liveness = Action {
    Ok(play.api.libs.json.Json.obj(
      "status" -> "alive",
      "timestamp" -> System.currentTimeMillis()
    ))
  }

  /**
   * Readiness probe - indicates if the application is ready to serve traffic
   * Should return 200 only if all critical dependencies (database, etc.) are available
   */
  def readiness = Action.async { implicit request =>
    // Check database connectivity
    val checkQuery = sql"SELECT 1".as[Int]
    
    dbConfig.db.run(checkQuery.headOption).map { result =>
      if (result.isDefined) {
        Ok(play.api.libs.json.Json.obj(
          "status" -> "ready",
          "database" -> "connected",
          "timestamp" -> System.currentTimeMillis()
        ))
      } else {
        ServiceUnavailable(play.api.libs.json.Json.obj(
          "status" -> "not_ready",
          "database" -> "disconnected",
          "timestamp" -> System.currentTimeMillis()
        ))
      }
    }.recover { case ex =>
      ServiceUnavailable(play.api.libs.json.Json.obj(
        "status" -> "not_ready",
        "database" -> "error",
        "error" -> ex.getMessage,
        "timestamp" -> System.currentTimeMillis()
      ))
    }
  }

  /**
   * General health check (backwards compatibility)
   * Combines liveness and readiness checks
   */
  def check = Action.async { implicit request =>
    readiness(request)
  }
}
