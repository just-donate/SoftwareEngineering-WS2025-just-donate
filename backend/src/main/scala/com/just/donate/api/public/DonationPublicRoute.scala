package com.just.donate.api.public

import cats.effect.IO
import com.just.donate.api.DonationRoute.StatusResponse
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.notify.IEmailService
import com.just.donate.utils.RouteUtils.loadOrganisation
import com.just.donate.utils.Money
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import com.just.donate.models.Donation
import java.time.LocalDateTime

object DonationPublicRoute:

  val donationRoute: (Repository[String, Organisation], Config, IEmailService) => HttpRoutes[IO] =
    (repository, config, emailService) =>
      HttpRoutes.of[IO]:

        case GET -> Root / organisationId / "donor" / donorId =>
          loadOrganisation[PublicDonationListResponse](organisationId)(repository): organisation =>
            PublicDonationListResponse(
              organisation.getDonations(donorId).map(toPublicDonationResponse(organisationId))
            )

  def toPublicDonationResponse(organisationId: String)(donation: Donation): PublicDonationResponse =
    PublicDonationResponse(
      donation.id,
      donation.amountTotal,
      organisationId,
      donation.donationDate,
      donation.earmarking.map(_.name),
      donation.statusUpdates.map: status =>
        StatusResponse(status.status.toString.toLowerCase, status.date, status.description)
    )

  private[api] case class PublicDonationResponse(
    donationId: String,
    amount: Money,
    organisation: String,
    date: LocalDateTime,
    earmarking: Option[String],
    status: Seq[StatusResponse]
  )

  private[api] case class PublicDonationListResponse(
    donations: Seq[PublicDonationResponse]
  )
