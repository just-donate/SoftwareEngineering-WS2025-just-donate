package com.just.donate.api

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.{ HttpRoutes, UrlForm }

object PaypalRoute:

  private val ipnInfo: String = "IPN Endpoint"

  val paypalRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root =>
      Ok(ipnInfo)

    case req @ POST -> Root =>
      for
        // Parse the body as URL-encoded form data
        body <- req.as[UrlForm].map(_.values.toMap)
        _ <- IO.println(s"Received IPN Payload: $body") // Log for debugging

        PaypalRoute.ipnInfo = body.toString()
        response <- Ok(body.toString())
      yield response
