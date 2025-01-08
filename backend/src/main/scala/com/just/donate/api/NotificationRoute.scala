package com.just.donate.api

import cats.effect.*
import com.just.donate.config.Config
import com.just.donate.notify.EmailService
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object NotificationRoute:

  val notificationRoute: Config => HttpRoutes[IO] = (config: Config) =>
    HttpRoutes.of[IO]:

      case req @ POST -> Root / donor =>
        for
          notification <- req.attemptAs[NotificationRequest].value
          _ <- notification match
            case Right(NotificationRequest(message)) => new EmailService(config).sendEmail(donor, message)
            case Left(_)                             => BadRequest()
          response <- Ok("Notification sent")
        yield response

  private case class NotificationRequest(message: String)
