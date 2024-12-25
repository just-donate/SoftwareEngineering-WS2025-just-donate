package com.just.donate.api

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.{HttpRoutes, UrlForm}

import scala.collection.mutable.ListBuffer

object PaypalRoute:

  private val ipnPayloads: ListBuffer[String] = ListBuffer.empty

  val paypalRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root =>
        Ok(ipnPayloads.mkString("\n"))

    case req @ POST -> Root =>
      for
        // Parse the body as URL-encoded form data
        body <- req.as[UrlForm].map(_.values.toMap)
        _ <- IO.println(s"Received IPN Payload: $body") // Log for debugging

        _ <- IO(ipnPayloads += body.toString())
        response <- Ok("IPN Payload received")
      yield response
