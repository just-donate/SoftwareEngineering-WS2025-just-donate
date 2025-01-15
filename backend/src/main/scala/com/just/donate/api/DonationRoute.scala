package com.just.donate.api

import cats.effect.*
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.errors.DonationError
import com.just.donate.models.{Donation, Donor, Earmarking, Organisation}
import com.just.donate.notify.IEmailService
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

  val donationRoute: (Repository[String, Organisation], Config, IEmailService) => HttpRoutes[IO] =
    (repository, config, emailService) =>
      HttpRoutes.of[IO]:

        case req @ POST -> Root / organisationId / "account" / accountName =>
          (for
            requestDonation <- req.as[RequestDonation]
            trackingId <- loadAndSaveOrganisationOps(organisationId)(repository)(
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

        case GET -> Root / organisationId / "donations" =>
          loadOrganisation[DonationListResponse](organisationId)(repository): organisation =>
            DonationListResponse(
              organisation.getDonations.map(toResponseDonation(organisationId))
            )

  def toResponseDonation(organisationId: String)(donation: Donation): DonationResponse =
    DonationResponse(
      donation.id,
      donation.amountTotal,
      organisationId,
      donation.donationDate,
      donation.earmarking.map(_.name),
      donation.statusUpdates.map: status =>
        StatusResponse(status.status.toString.toLowerCase, status.date, status.description)
    )

  val emailTemplate: (String, String, String) => String = (linkWithId, id, link) =>
    f"""Thank you for your donation, to track your progress visit
       |$linkWithId
       |or enter your tracking id
       |$id
       |on our tracking page
       |$link""".stripMargin

  def organisationMapper(requestDonation: RequestDonation, accountName: String)(
    org: Organisation
  ): (Organisation, Either[DonationError, String]) =
    val donor = org
      .getExistingDonor(requestDonation.donorEmail)
      .getOrElse(
        Donor(org.getNewDonorId, requestDonation.donorName, requestDonation.donorEmail)
      )

    val earmarking = requestDonation.earmarking match
      case Some(value) =>
        org.getEarmarking(value) match
          case Some(value) => Some(value)
          case None        => return (org, Left(DonationError.INVALID_EARMARKING))
      case None => None

    val (donation, donationPart) = earmarking match
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
    earmarking: Option[String],
    status: Seq[StatusResponse]
  )

  private[api] case class DonationListResponse(
    donations: Seq[DonationResponse]
  )
