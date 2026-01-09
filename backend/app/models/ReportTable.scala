package models

import slick.jdbc.PostgresProfile.api._
import play.api.libs.json._
import java.time.Instant

case class Report(id: Long, title: String, content: String, createdAt: Instant)

object Report {
  // JSON format for Instant (ISO 8601 string)
  implicit val instantReads: Reads[Instant] = {
    Reads[Instant](js => js.validate[String].map(Instant.parse))
  }
  implicit val instantWrites: Writes[Instant] = {
    Writes[Instant](instant => JsString(instant.toString))
  }
  implicit val instantFormat: Format[Instant] = Format(instantReads, instantWrites)
  implicit val format: Format[Report] = Json.format[Report]
}

class ReportsTable(tag: Tag) extends Table[Report](tag, "reports") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def content = column[String]("content")
  def createdAt = column[Instant]("created_at")
  
  def * = (id, title, content, createdAt).mapTo[Report]
}

object ReportsTable {
  val reports = TableQuery[ReportsTable]
}
