package com.just.donate.api

import cats.effect.*
import com.just.donate.notify.SendEmail
import com.just.donate.store.FileStore
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object NotificationRoute:

  val notificationRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:

    case req @ POST -> Root / "notify" / donor =>
      for
        notification <- req.attemptAs[NotificationRequest].value
        _ <- notification match
          case Right(NotificationRequest(message)) => SendEmail.sendEmail(donor, message)
          case Left(_)                             => SendEmail.sendEmail(donor)
        response <- Ok("Notification sent")
      yield response

  private case class NotificationRequest(message: String)
