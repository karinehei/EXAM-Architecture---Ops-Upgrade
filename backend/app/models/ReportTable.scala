package models

import slick.jdbc.PostgresProfile.api._
import play.api.libs.json._
import java.time.Instant

case class Report(id: Long, title: String, content: String, createdAt: Instant)

object Report {
  implicit val instantFormat: Format[Instant] = Format(
    Reads.instantReads,
    Writes.instantWrites
  )
  implicit val format: Format[Report] = Json.format[Report]
}

class ReportsTable(tag: Tag) extends Table[Report](tag, "reports") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def content = column[String]("content")
  def createdAt = column[Instant]("created_at")
  
  def * = (id, title, content, createdAt) <> (Report.tupled, Report.unapply)
}

object ReportsTable {
  val reports = TableQuery[ReportsTable]
}
