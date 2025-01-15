package com.just.donate.api

import cats.effect.*
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.notify.IEmailService
import com.just.donate.notify.EmailMessage
import com.just.donate.notify.messages.ManualMessage
import com.just.donate.utils.RouteUtils.loadOrganisationOps
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object NotificationRoute:

  val notificationRoute: (Repository[String, Organisation], Config, IEmailService) => HttpRoutes[IO] =
    (repository, config, emailService) =>
      HttpRoutes.of[IO]:

        case req @ POST -> Root / organisationId / donationId =>
          for
            notification <- req.attemptAs[NotificationRequest].value
            notificationResult <- loadOrganisationOps(organisationId)(repository): organisation =>
              (notification, organisation.donations.get(donationId)) match
                case (Left(_), _)     => BadRequest("Invalid request body")
                case (Right(_), None) => BadRequest("Donation not found")
                case (Right(NotificationRequest(message)), Some(donation)) =>
                  organisation.donors.get(donation.donorId) match
                    case None => BadRequest("Donor not found")
                    case Some(donor) =>
                      emailService.sendEmail(
                        donor.email,
                        EmailMessage.prepareString(
                          Some(message),
                          ManualMessage(
                            donor,
                            config,
                            organisation.name
                          )
                        ),
                        "Just Donate: A notification for your donation"
                      ) >> Ok("Notification sent")
            response <- notificationResult.getOrElse(NotFound())
          yield response

  private case class NotificationRequest(message: String)
