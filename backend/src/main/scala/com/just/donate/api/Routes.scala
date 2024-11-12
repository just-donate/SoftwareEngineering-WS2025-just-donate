package com.just.donate.api

import cats.data.Kleisli
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*

object Routes:

  val api: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root => Ok("Hello, World!")

  }


