package com.just.donate.api.public

import cats.effect.IO
import com.just.donate.api.DonationRoute.{ DonationListResponse, DonationResponse, toResponseDonation }
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.notify.IEmailService
import com.just.donate.utils.RouteUtils.loadOrganisationOps
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.io.*

object DonationPublicRoute:

  val donationRoute: (Repository[String, Organisation], Config, IEmailService) => HttpRoutes[IO] =
    (repository, config, emailService) =>
      HttpRoutes.of[IO]:

        case GET -> Root / organisationId / "donor" / donorId =>
          for
            donationResult <- loadOrganisationOps(organisationId)(repository): organisation =>
              organisation.getDonations.foldLeft[Option[Seq[DonationResponse]]](Some(Seq[DonationResponse]())) {
                (optDonations, donation) =>
                  optDonations match
                    case None => None
                    case Some(responseDonations) =>
                      organisation.donors.get(donation.donorId) match
                        case None => None
                        case Some(donor) =>
                          Some(responseDonations :+ toResponseDonation(organisationId, donation, donor))
              }
            reponse <- donationResult match
              case None                  => NotFound("Organisation not found")
              case Some(None)            => InternalServerError("Found donation with invalid donor")
              case Some(Some(donations)) => Ok(DonationListResponse(donations))
          yield reponse
