package com.just.donate.api.public

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.MediaType
import org.http4s.headers.`Content-Type`
import org.http4s.Response
import org.http4s.implicits._
import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.StaticFile
import org.http4s.Request
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global

object SwaggerUiRoute {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "swagger-ui" =>
      // The HTML page that loads Swagger UI and points to the OpenAPI YAML
      val html: String =
        """
          |<!DOCTYPE html>
          |<html lang="en">
          |  <head>
          |    <meta charset="UTF-8">
          |    <title>just-donate API Docs</title>
          |    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist/swagger-ui.css" />
          |    <style>
          |      html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
          |      *, *:before, *:after { box-sizing: inherit; }
          |      body { margin:0; background: #fafafa; }
          |    </style>
          |  </head>
          |  <body>
          |    <div id="swagger-ui"></div>
          |    <script src="https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js"></script>
          |    <script>
          |      const ui = SwaggerUIBundle({
          |        url: "/api-docs/openapi.yml",
          |        dom_id: '#swagger-ui',
          |        presets: [
          |          SwaggerUIBundle.presets.apis,
          |          SwaggerUIBundle.SwaggerUIStandalonePreset
          |        ],
          |        layout: "BaseLayout",
          |      });
          |    </script>
          |  </body>
          |</html>
          |""".stripMargin
      Ok(html).map(_.withHeaders(`Content-Type`(MediaType.text.html)))

    case GET -> Root / "openapi.yml" =>
      // Adjust the resource path according to your project structure
      StaticFile.fromResource("openapi.yml", Some(Request[IO]())).getOrElseF(NotFound())
  }


}

