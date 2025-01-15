package com.just.donate.api.public

import cats.effect.IO
import com.just.donate.api.DonationRoute.{DonationListResponse, toResponseDonation}
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.notify.IEmailService
import com.just.donate.utils.RouteUtils.loadOrganisation
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*

object DonationPublicRoute:

  val donationRoute: (Repository[String, Organisation], Config, IEmailService) => HttpRoutes[IO] =
    (repository, config, emailService) =>
      HttpRoutes.of[IO]:

        case GET -> Root / organisationId / "donor" / donorId
        =>
          loadOrganisation[DonationListResponse](organisationId)(repository): organisation =>
            DonationListResponse(
              organisation.getDonations(donorId).map(toResponseDonation(organisationId, organisation.name))
            )
