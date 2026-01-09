package repositories

import javax.inject.Inject
import models.{Report, ReportsTable}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class ReportRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db
  private val reports = TableQuery[ReportsTable]

  def findAll(page: Int = 1, pageSize: Int = 20, filters: Option[String] = None): Future[(Seq[Report], Int)] = {
    val offset = (page - 1) * pageSize
    var query = reports
    
    // Apply filters if provided
    val filteredQuery = filters match {
      case Some(filter) if filter.nonEmpty =>
        query.filter(r => r.title.like(s"%$filter%") || r.content.like(s"%$filter%"))
      case _ => query
    }
    
    val countQuery = filteredQuery.length.result
    val dataQuery = filteredQuery
      .sortBy(_.createdAt.desc)
      .drop(offset)
      .take(pageSize)
      .result
    
    for {
      total <- db.run(countQuery)
      data <- db.run(dataQuery)
    } yield (data, total)
  }

  def findById(id: Long): Future[Option[Report]] = {
    db.run(reports.filter(_.id === id).result.headOption)
  }

  def create(report: Report): Future[Report] = {
    val insertQuery = (reports returning reports.map(_.id) into ((report, id) => report.copy(id = id))) += report
    db.run(insertQuery)
  }

  def update(id: Long, report: Report): Future[Option[Report]] = {
    val updateQuery = reports.filter(_.id === id).update(report.copy(id = id))
    db.run(updateQuery).flatMap { rowsAffected =>
      if (rowsAffected > 0) {
        findById(id)
      } else {
        Future.successful(None)
      }
    }
  }

  def delete(id: Long): Future[Boolean] = {
    db.run(reports.filter(_.id === id).delete).map(_ > 0)
  }
}
