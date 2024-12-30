package com.just.donate.api

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*

object RootRoute:

  val api: HttpRoutes[IO] = HttpRoutes.of[IO]:

    case GET -> Root => Ok("Hello, World!")

    case GET -> Root / "hello" / name => Ok(s"Hello, $name!")

    case req@POST -> Root / "bounce" => for
      data <- req.as[String]
      response <- Ok(s"Got: $data")
    yield response
