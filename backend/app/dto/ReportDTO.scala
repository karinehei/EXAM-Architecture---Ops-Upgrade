package dto

import play.api.libs.json._
import java.time.Instant

// Request DTOs
case class CreateReportRequest(title: String, content: String)
object CreateReportRequest {
  implicit val format: Format[CreateReportRequest] = Json.format[CreateReportRequest]
}

case class UpdateReportRequest(title: String, content: String)
object UpdateReportRequest {
  implicit val format: Format[UpdateReportRequest] = Json.format[UpdateReportRequest]
}

// Response DTOs
case class ReportResponse(id: Long, title: String, content: String, createdAt: Instant)
object ReportResponse {
  implicit val format: Format[ReportResponse] = Json.format[ReportResponse]
  
  def fromModel(report: models.Report): ReportResponse = {
    ReportResponse(report.id, report.title, report.content, report.createdAt)
  }
}

case class PaginatedResponse[T](data: Seq[T], pagination: PaginationInfo)
object PaginatedResponse {
  implicit def format[T: Format]: Format[PaginatedResponse[T]] = Json.format[PaginatedResponse[T]]
}

case class PaginationInfo(page: Int, pageSize: Int, total: Int, totalPages: Int)
object PaginationInfo {
  implicit val format: Format[PaginationInfo] = Json.format[PaginationInfo]
  
  def apply(page: Int, pageSize: Int, total: Int): PaginationInfo = {
    val totalPages = if (total > 0) Math.ceil(total.toDouble / pageSize).toInt else 0
    PaginationInfo(page, pageSize, total, totalPages)
  }
}

case class ErrorResponse(error: String, message: String, details: Option[String] = None)
object ErrorResponse {
  implicit val format: Format[ErrorResponse] = Json.format[ErrorResponse]
}
