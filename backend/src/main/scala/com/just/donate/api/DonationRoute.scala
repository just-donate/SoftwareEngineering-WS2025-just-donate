package com.just.donate.api

import cats.effect.*
import com.just.donate.config.Config
import com.just.donate.models.{ Donation, DonationError, Donor, Organisation }
import com.just.donate.notify.IEmailService
import com.just.donate.store.Store
import com.just.donate.utils.RouteUtils.loadAndSaveOrganisation
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*
import cats.implicits.*

object DonationRoute:
  val donationRoute: (Store, Config, IEmailService) => HttpRoutes[IO] = (store, config, emailService) =>
    HttpRoutes.of[IO]:
      case req @ POST -> Root / organisationId / "account" / accountName / "donate" =>
        var trackingId = ""
        var trackingLink = config.frontendUrl
        for
          requestDonation <- req.as[RequestDonation]
          response <- loadAndSaveOrganisation(organisationId)(store)(org =>
            val existingDonor = org.getExistingDonor(requestDonation.donorEmail)
            val donor = existingDonor.getOrElse(
              Donor(org.getNewDonorId, requestDonation.donorName, requestDonation.donorEmail)
            )
            val donationPart = requestDonation.earmarking match
              case Some(earmarking) => Donation(donor.id, requestDonation.amount, earmarking)
              case None             => Donation(donor.id, requestDonation.amount)

            val newOrg = org.donate(donor, donationPart, accountName) match
              case Left(DonationError.INVALID_ACCOUNT)    => throw new RuntimeException("invalid account")
              case Left(DonationError.INVALID_EARMARKING) => throw new RuntimeException("invalid earmarking")
              case Right(value)                           => value

            trackingId = donationPart.donation.donorId
            trackingLink = f"${trackingLink}/tracking?id=${trackingId}"

            newOrg
          )
          _ <- emailService.sendEmail(
            requestDonation.donorEmail,
            f"""Thank you for your donation, to track your progress visit
               |${trackingLink}
               |or enter your tracking id
               |${trackingId}
               |on our tracking page
               |${config.frontendUrl}""".stripMargin
          )
        yield response

  private[api] case class RequestDonation(
    donorName: String,
    donorEmail: String,
    amount: BigDecimal,
    earmarking: Option[String]
  )
