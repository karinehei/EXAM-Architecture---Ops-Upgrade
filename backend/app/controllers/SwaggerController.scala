package controllers

import javax.inject._
import play.api.mvc._
import play.api.Environment

/**
 * Swagger UI Controller
 * 
 * Serves Swagger UI for interactive API documentation.
 * The OpenAPI spec is loaded from the docs/api/openapi.yaml file.
 */
@Singleton
class SwaggerController @Inject()(
  val controllerComponents: ControllerComponents,
  environment: Environment
) extends BaseController {

  /**
   * Swagger UI page
   * Serves an HTML page with embedded Swagger UI
   */
  def ui = Action { implicit request =>
    val swaggerHtml = s"""
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>EXAM System API - Swagger UI</title>
  <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui.css" />
  <style>
    html {
      box-sizing: border-box;
      overflow: -moz-scrollbars-vertical;
      overflow-y: scroll;
    }
    *, *:before, *:after {
      box-sizing: inherit;
    }
    body {
      margin:0;
      background: #fafafa;
    }
  </style>
</head>
<body>
  <div id="swagger-ui"></div>
  <script src="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-bundle.js"></script>
  <script src="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-standalone-preset.js"></script>
  <script>
    window.onload = function() {
      const ui = SwaggerUIBundle({
        url: "/api-docs/openapi.yaml",
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
      });
    };
  </script>
</body>
</html>
"""
    Ok(swaggerHtml).as("text/html")
  }

  /**
   * Serves the OpenAPI YAML specification
   */
  def openapiYaml = Action {
    // Try to load from conf/api-docs (for Docker) or docs/api (for local dev)
    val openapiPath = environment.getFile("conf/api-docs/openapi.yaml")
    val fallbackPath = environment.getFile("../../docs/api/openapi.yaml")
    
    val file = if (openapiPath.exists()) {
      openapiPath
    } else if (fallbackPath.exists()) {
      fallbackPath
    } else {
      null
    }
    
    if (file != null) {
      val content = scala.io.Source.fromFile(file, "UTF-8").mkString
      Ok(content).as("text/yaml")
    } else {
      // Return a basic OpenAPI spec if file not found
      val basicSpec = """openapi: 3.0.3
info:
  title: EXAM System API
  version: 1.0.0
servers:
  - url: http://localhost:9000
paths:
  /api/v1/reports:
    get:
      summary: List reports
      responses:
        '200':
          description: Success
"""
      Ok(basicSpec).as("text/yaml")
    }
  }
}
