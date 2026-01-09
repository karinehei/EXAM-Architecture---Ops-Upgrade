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
    
    // Apply filters if provided
    // Note: For better performance with full-text search, we could use PostgreSQL's
    // full-text search functions (to_tsvector, to_tsquery), but LIKE is simpler
    // and the GIN index will still help with pattern matching
    val filteredQuery = filters match {
      case Some(filter) if filter.nonEmpty =>
        reports.filter(r => r.title.like(s"%$filter%") || r.content.like(s"%$filter%"))
      case _ => reports
    }
    
    // Optimize count query: use a subquery to avoid materializing all rows
    // The GIN index on title+content will help with filter performance
    val countQuery = filteredQuery.length.result
    
    // Data query with sorting and pagination
    // The DESC index on created_at will optimize sorting
    val dataQuery = filteredQuery
      .sortBy(_.createdAt.desc)
      .drop(offset)
      .take(pageSize)
      .result
    
    // Run queries in parallel for better performance
    // Both queries can benefit from the indexes we've created
    for {
      (total, data) <- db.run(countQuery).zip(db.run(dataQuery))
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
