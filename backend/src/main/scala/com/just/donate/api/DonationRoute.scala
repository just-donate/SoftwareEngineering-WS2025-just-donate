package com.just.donate.api

import cats.effect.*
import cats.implicits.*
import com.just.donate.config.Config
import com.just.donate.models.errors.{ DonationError, TransferError, WithdrawError }
import com.just.donate.models.{ Donation, Donor, Organisation }
import com.just.donate.notify.IEmailService
import com.just.donate.store.Store
import com.just.donate.utils.RouteUtils.loadAndSaveOrganisationOps
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object DonationRoute:
  val donationRoute: (Store, Config, IEmailService) => HttpRoutes[IO] = (store, config, emailService) =>
    HttpRoutes.of[IO]:

      case req @ POST -> Root / organisationId / "account" / accountName =>
        (for
          requestDonation <- req.as[RequestDonation]
          trackingId <- loadAndSaveOrganisationOps(organisationId)(store)(
            organisationMapper(requestDonation, accountName)
          )
          response <- trackingId match
            case None                      => BadRequest("Organisation not found")
            case Some(Left(donationError)) => BadRequest(donationError.message)
            case Some(Right(trackingId)) =>
              val trackingLink = f"${config.frontendUrl}/tracking?id=${trackingId}"
              emailService.sendEmail(
                requestDonation.donorEmail,
                emailTemplate(trackingLink, trackingId, config.frontendUrl)
              ) >> Ok()
        yield response).handleErrorWith {
          case e: InvalidMessageBodyFailure => BadRequest(e.getMessage)
        }

  private val emailTemplate: (String, String, String) => String = (linkWithId, id, link) =>
    f"""Thank you for your donation, to track your progress visit
       |${linkWithId}
       |or enter your tracking id
       |${id}
       |on our tracking page
       |${link}""".stripMargin

  private def organisationMapper(requestDonation: RequestDonation, accountName: String)(
    org: Organisation
  ): (Organisation, Either[DonationError, String]) =
    val existingDonor = org.getExistingDonor(requestDonation.donorEmail)
    val donor = existingDonor.getOrElse(
      Donor(org.getNewDonorId, requestDonation.donorName, requestDonation.donorEmail)
    )
    val (donation, donationPart) = requestDonation.earmarking match
      case Some(earmarking) => Donation(donor.id, requestDonation.amount, earmarking)
      case None             => Donation(donor.id, requestDonation.amount)

    org.donate(donor, donationPart, donation, accountName) match
      case Left(error)   => (org, Left(error))
      case Right(newOrg) => (newOrg, Right(donor.id))

  private[api] case class RequestDonation(
    donorName: String,
    donorEmail: String,
    amount: BigDecimal,
    earmarking: Option[String]
  )
