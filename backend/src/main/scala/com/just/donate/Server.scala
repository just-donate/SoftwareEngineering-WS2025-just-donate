package com.just.donate

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.just.donate.api.Routes.api
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.staticcontent.{FileService, fileService}
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jFactory

import scala.concurrent.duration.*

object Server extends IOApp:

  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  // Serve from resources folder /public
  private val httpApp: HttpApp[IO] = Router(
    "" -> fileService[IO](FileService.Config("../frontend/dist")),
    "api" -> api
  ).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
