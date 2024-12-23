package com.just.donate

import cats.effect.*
import com.comcast.ip4s.*
import com.just.donate.api.OrganisationRoute.organisationApi
import com.just.donate.store.FileStore
import org.http4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.staticcontent.{FileService, fileService}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Server extends IOApp:

  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  // Serve from resources folder /public
  private val httpApp: HttpApp[IO] = Router(
    "" -> fileService[IO](FileService.Config("../frontend/dist")),
    "organisation" -> organisationApi(FileStore)
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
