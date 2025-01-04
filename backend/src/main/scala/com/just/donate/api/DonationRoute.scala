package com.just.donate.api

import cats.effect.*
import com.just.donate.config.Config
import com.just.donate.models.Donation
import com.just.donate.notify.IEmailService
import com.just.donate.store.Store
import com.just.donate.utils.RouteUtils.loadAndSaveOrganisation
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object DonationRoute:
  val donationRoute: (Store, Config, IEmailService) => HttpRoutes[IO] = (store, config, emailService) =>
    HttpRoutes.of[IO]:
      case req@POST -> Root / organisationId / "account" / accountName / "donate" =>
        for
          requestDonation <- req.as[RequestDonation]
          donationPart <- IO(requestDonation.earmarking match
            case Some(earmarking) => Donation(requestDonation.donor, requestDonation.amount, earmarking)
            case None => Donation(requestDonation.donor, requestDonation.amount))
          response <- loadAndSaveOrganisation(organisationId)(store)(
            _.donate(donationPart, accountName)
          )
          trackingId <- IO(donationPart.donation.donorId)
          trackingLink <- IO(f"${config.frontendUrl}/tracking?id=${trackingId}")
          _ <- emailService.sendEmail(
            requestDonation.donor,
            f"""Thank you for your donation, to track your progress visit
               |${trackingLink}
               |or enter your tracking id
               |${trackingId}
               |on our tracking page
               |${config.frontendUrl}""".stripMargin
          )
        yield response

  private[api] case class RequestDonation(donor: String, amount: BigDecimal, earmarking: Option[String])