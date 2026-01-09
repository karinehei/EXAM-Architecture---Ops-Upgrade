package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import repositories.ReportRepository
import dto._
import models.Report
import java.time.Instant

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReportController @Inject()(
  val controllerComponents: ControllerComponents,
  reportRepository: ReportRepository
)(implicit ec: ExecutionContext) extends BaseController {

  def list(filters: Option[String], page: Option[Int], pageSize: Option[Int]) = Action.async { implicit request =>
    val pageNum = Math.max(1, page.getOrElse(1))
    val size = Math.min(100, Math.max(1, pageSize.getOrElse(20))) // Limit page size to 100
    
    reportRepository.findAll(pageNum, size, filters).map { case (reports, total) =>
      val reportResponses = reports.map(ReportResponse.fromModel)
      val pagination = PaginationInfo(pageNum, size, total)
      val response = PaginatedResponse(reportResponses, pagination)
      Ok(Json.toJson(response))
    }.recover {
      case ex: Exception =>
        InternalServerError(Json.toJson(ErrorResponse(
          error = "INTERNAL_ERROR",
          message = "Failed to retrieve reports",
          details = Some(ex.getMessage)
        )))
    }
  }

  def get(id: Long) = Action.async { implicit request =>
    reportRepository.findById(id).map {
      case Some(report) => Ok(Json.toJson(ReportResponse.fromModel(report)))
      case None => NotFound(Json.toJson(ErrorResponse(
        error = "NOT_FOUND",
        message = s"Report with id $id not found"
      )))
    }.recover {
      case ex: Exception =>
        InternalServerError(Json.toJson(ErrorResponse(
          error = "INTERNAL_ERROR",
          message = "Failed to retrieve report",
          details = Some(ex.getMessage)
        )))
    }
  }

  def create = Action.async(parse.json) { implicit request =>
    request.body.validate[CreateReportRequest].fold(
      errors => {
        Future.successful(BadRequest(Json.toJson(ErrorResponse(
          error = "VALIDATION_ERROR",
          message = "Invalid request body",
          details = Some(errors.toString())
        ))))
      },
      createRequest => {
        val now = Instant.now()
        val report = Report(0, createRequest.title, createRequest.content, now)
        reportRepository.create(report).map { createdReport =>
          Created(Json.toJson(ReportResponse.fromModel(createdReport)))
            .withHeaders(LOCATION -> routes.ReportController.get(createdReport.id).url)
        }.recover {
          case ex: Exception =>
            InternalServerError(Json.toJson(ErrorResponse(
              error = "INTERNAL_ERROR",
              message = "Failed to create report",
              details = Some(ex.getMessage)
            )))
        }
      }
    )
  }

  def update(id: Long) = Action.async(parse.json) { implicit request =>
    request.body.validate[UpdateReportRequest].fold(
      errors => {
        Future.successful(BadRequest(Json.toJson(ErrorResponse(
          error = "VALIDATION_ERROR",
          message = "Invalid request body",
          details = Some(errors.toString())
        ))))
      },
      updateRequest => {
        reportRepository.findById(id).flatMap {
          case Some(existing) =>
            val updated = existing.copy(title = updateRequest.title, content = updateRequest.content)
            reportRepository.update(id, updated).map {
              case Some(report) => Ok(Json.toJson(ReportResponse.fromModel(report)))
              case None => NotFound(Json.toJson(ErrorResponse(
                error = "NOT_FOUND",
                message = s"Report with id $id not found"
              )))
            }
          case None =>
            Future.successful(NotFound(Json.toJson(ErrorResponse(
              error = "NOT_FOUND",
              message = s"Report with id $id not found"
            ))))
        }.recover {
          case ex: Exception =>
            InternalServerError(Json.toJson(ErrorResponse(
              error = "INTERNAL_ERROR",
              message = "Failed to update report",
              details = Some(ex.getMessage)
            )))
        }
      }
    )
  }

  def delete(id: Long) = Action.async { implicit request =>
    reportRepository.delete(id).map { deleted =>
      if (deleted) {
        NoContent
      } else {
        NotFound(Json.toJson(ErrorResponse(
          error = "NOT_FOUND",
          message = s"Report with id $id not found"
        )))
      }
    }.recover {
      case ex: Exception =>
        InternalServerError(Json.toJson(ErrorResponse(
          error = "INTERNAL_ERROR",
          message = "Failed to delete report",
          details = Some(ex.getMessage)
        )))
    }
  }
}
