package com.just.donate.api

import cats.effect.*
import com.just.donate.config.Config
import com.just.donate.models.errors.DonationError
import com.just.donate.models.{Donation, Donor, Organisation}
import com.just.donate.notify.IEmailService
import com.just.donate.store.Store
import com.just.donate.utils.Money
import com.just.donate.utils.RouteUtils.{loadAndSaveOrganisationOps, loadOrganisation}
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

import java.time.LocalDateTime

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
              val trackingLink = f"${config.frontendUrl}/tracking?id=$trackingId"
              emailService.sendEmail(
                requestDonation.donorEmail,
                emailTemplate(trackingLink, trackingId, config.frontendUrl)
              ) >> Ok()
        yield response).handleErrorWith {
          case e: InvalidMessageBodyFailure => BadRequest(e.getMessage)
        }

      case GET -> Root / organisationId / "donor" / donorId =>
        loadOrganisation[DonationListResponse](organisationId)(store): organisation =>
          DonationListResponse(
            organisation
              .getDonations(donorId)
              .map: donation =>
                DonationResponse(
                  donation.id,
                  donation.amountTotal,
                  organisation.name,
                  donation.donationDate,
                  donation.earmarking.getOrElse(""),
                  donation.statusUpdates.map: status =>
                    StatusResponse(status.status.toString.toLowerCase, status.date, status.description)
                )
          )

  private val emailTemplate: (String, String, String) => String = (linkWithId, id, link) =>
    f"""Thank you for your donation, to track your progress visit
       |$linkWithId
       |or enter your tracking id
       |$id
       |on our tracking page
       |$link""".stripMargin

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
    amount: Money,
    earmarking: Option[String]
  )

  // Define the Status case class to represent each status update
  private[api] case class StatusResponse(
    status: String,
    date: LocalDateTime,
    description: String
  )

  // Define the Donation case class to represent each donation
  private[api] case class DonationResponse(
    donationId: String,
    amount: Money,
    organisation: String,
    date: LocalDateTime,
    earmarking: String,
    status: Seq[StatusResponse]
  )

  private[api] case class DonationListResponse(
    donations: Seq[DonationResponse]
  )
