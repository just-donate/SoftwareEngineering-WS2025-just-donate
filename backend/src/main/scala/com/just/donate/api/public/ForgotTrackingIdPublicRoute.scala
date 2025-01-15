package com.just.donate.api.public

import cats.effect.IO
import com.just.donate.api.DonationRoute.{ DonationListResponse, toResponseDonation }
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.notify.EmailMessage
import com.just.donate.notify.IEmailService
import com.just.donate.notify.messages.ForgotTrackingIdMessage
import com.just.donate.utils.RouteUtils.loadOrganisationOps
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object ForgotTrackingIdPublicRoute:

  val publicApi: (Repository[String, Organisation], Config, IEmailService) => HttpRoutes[IO] =
    (repository, config, emailService) =>
      HttpRoutes.of[IO]:

        case req @ POST -> Root / organisationId =>
          for
            requestDonation <- req.attemptAs[ForgotTrackingIdRequest].value
            forgotTrackingIdResult <- loadOrganisationOps(organisationId)(repository): organisation =>
              requestDonation match
                case Left(_) => BadRequest("Invalid request body")
                case Right(ForgotTrackingIdRequest(email)) =>
                  organisation.getExistingDonor(email) match
                    case None => BadRequest("Donation not found")
                    case Some(donor) =>
                      emailService.sendEmail(
                        donor.email,
                        EmailMessage.prepareString(
                          None,
                          ForgotTrackingIdMessage(
                            donor,
                            config
                          )
                        ),
                        "Just Donate: Your tracking id"
                      ) >> Ok()
            response <- forgotTrackingIdResult.getOrElse(NotFound())
          yield response

  private[api] case class ForgotTrackingIdRequest(email: String)
