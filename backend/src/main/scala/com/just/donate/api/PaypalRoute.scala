package com.just.donate.api

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.{ HttpRoutes, UrlForm }

object PaypalRoute:

  val paypalRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root =>
      Ok("IPN Endpoint")

    case req @ POST -> Root =>
      for
        // Parse the body as URL-encoded form data
        body <- req.as[UrlForm].map(_.values.toMap)
        _ <- IO.println(s"Received IPN Payload: $body") // Log for debugging

        response <- Ok(body.toString())
      yield response
