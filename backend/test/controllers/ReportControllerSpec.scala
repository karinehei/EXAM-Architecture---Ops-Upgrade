package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import dto._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class ReportControllerSpec extends PlaySpec with OneAppPerTest {
  
  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "play.http.router" -> "router.Routes",
        "play.application.loader" -> "app.ExamApplicationLoader",
        // Use H2 in-memory database for tests (PostgreSQL compatibility mode)
        "slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$",
        "slick.dbs.default.db.driver" -> "org.h2.Driver",
        "slick.dbs.default.db.url" -> "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE SCHEMA IF NOT EXISTS public",
        "slick.dbs.default.db.user" -> "sa",
        "slick.dbs.default.db.password" -> "",
        // Enable evolutions for tests
        "play.evolutions.enabled" -> "true",
        "play.evolutions.autoApply" -> "true"
      )
      .build()
  }

  "ReportController" should {

    "return paginated list of reports" in {
      val request = FakeRequest(GET, "/api/v1/reports?page=1&pageSize=10")
      val response = route(app, request).get

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val json = contentAsJson(response)
      // Empty list is valid for a fresh database
      (json \ "data").as[JsArray] mustBe a[JsArray]
      (json \ "pagination" \ "page").as[Int] mustBe 1
      (json \ "pagination" \ "pageSize").as[Int] mustBe 10
    }

    "return 404 for non-existent report" in {
      val request = FakeRequest(GET, "/api/v1/reports/99999")
      val response = route(app, request).get

      status(response) mustBe NOT_FOUND
      contentType(response) mustBe Some("application/json")
      val json = contentAsJson(response)
      (json \ "error").as[String] mustBe "NOT_FOUND"
    }

    "create a new report" in {
      val createRequest = Json.obj(
        "title" -> "Test Report",
        "content" -> "Test content"
      )
      val request = FakeRequest(POST, "/api/v1/reports")
        .withJsonBody(createRequest)
        .withHeaders(CONTENT_TYPE -> "application/json")
      val response = route(app, request).get

      status(response) mustBe CREATED
      contentType(response) mustBe Some("application/json")
      val json = contentAsJson(response)
      (json \ "id").as[Long] must be > 0L
      (json \ "title").as[String] mustBe "Test Report"
      (json \ "content").as[String] mustBe "Test content"
    }

    "return 400 for invalid create request" in {
      val invalidRequest = Json.obj(
        "title" -> "Test" // missing content
      )
      val request = FakeRequest(POST, "/api/v1/reports")
        .withJsonBody(invalidRequest)
        .withHeaders(CONTENT_TYPE -> "application/json")
      val response = route(app, request).get

      status(response) mustBe BAD_REQUEST
      contentType(response) mustBe Some("application/json")
      val json = contentAsJson(response)
      (json \ "error").as[String] mustBe "VALIDATION_ERROR"
    }

    "update an existing report" in {
      // First create a report
      val createRequest = Json.obj(
        "title" -> "Original Title",
        "content" -> "Original content"
      )
      val createResponse = route(app, FakeRequest(POST, "/api/v1/reports")
        .withJsonBody(createRequest)
        .withHeaders(CONTENT_TYPE -> "application/json")).get
      val reportId = (contentAsJson(createResponse) \ "id").as[Long]

      // Then update it
      val updateRequest = Json.obj(
        "title" -> "Updated Title",
        "content" -> "Updated content"
      )
      val updateResponse = route(app, FakeRequest(PUT, s"/api/v1/reports/$reportId")
        .withJsonBody(updateRequest)
        .withHeaders(CONTENT_TYPE -> "application/json")).get

      status(updateResponse) mustBe OK
      val json = contentAsJson(updateResponse)
      (json \ "title").as[String] mustBe "Updated Title"
      (json \ "content").as[String] mustBe "Updated content"
    }

    "delete a report" in {
      // First create a report
      val createRequest = Json.obj(
        "title" -> "To Delete",
        "content" -> "This will be deleted"
      )
      val createResponse = route(app, FakeRequest(POST, "/api/v1/reports")
        .withJsonBody(createRequest)
        .withHeaders(CONTENT_TYPE -> "application/json")).get
      val reportId = (contentAsJson(createResponse) \ "id").as[Long]

      // Then delete it
      val deleteResponse = route(app, FakeRequest(DELETE, s"/api/v1/reports/$reportId")).get

      status(deleteResponse) mustBe NO_CONTENT

      // Verify it's deleted
      val getResponse = route(app, FakeRequest(GET, s"/api/v1/reports/$reportId")).get
      status(getResponse) mustBe NOT_FOUND
    }
  }
}
