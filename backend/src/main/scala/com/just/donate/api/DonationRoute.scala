package com.just.donate.api

import cats.effect.*
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.errors.DonationError
import com.just.donate.models.{ Donation, Donor, Earmarking, Organisation }
import com.just.donate.notify.EmailMessage
import com.just.donate.notify.IEmailService
import com.just.donate.notify.messages.DonationMessage
import com.just.donate.utils.Money
import com.just.donate.utils.RouteUtils.{ loadAndSaveOrganisationOps, loadOrganisation, loadOrganisationOps }
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
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
            donationResult <- loadAndSaveOrganisationOps(organisationId)(repository)(
              organisationMapper(requestDonation, accountName, config)
            )
            response <- donationResult match
              case None                      => BadRequest("Organisation not found")
              case Some(Left(donationError)) => BadRequest(donationError.message)
              case Some(Right((org, donor))) =>
                emailService.sendEmail(
                  requestDonation.donorEmail,
                  EmailMessage.prepareString(
                    org.theme.map(_.emailTemplates.donationTemplate),
                    DonationMessage(
                      donor,
                      config
                    )
                  )
                ) >> Ok()
          yield response).handleErrorWith {
            case e: (InvalidMessageBodyFailure | MalformedMessageBodyFailure) =>
              BadRequest(e.getMessage)
          }

        case GET -> Root / organisationId / "donations" =>
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
            response <- donationResult match
              case None                  => NotFound("Organisation not found")
              case Some(None)            => InternalServerError("Found donation with invalid donor")
              case Some(Some(donations)) => Ok(DonationListResponse(donations))
          yield response

  private def toResponseDonation(organisationId: String, donation: Donation, donor: Donor): DonationResponse =
    DonationResponse(
      donation.id,
      DonorResponse(donor.id, donor.name, donor.email),
      donation.amountTotal,
      organisationId,
      donation.donationDate,
      donation.earmarking.map(_.name),
      donation.statusUpdates.map: status =>
        StatusResponse(status.status.toString.toLowerCase, status.date, status.description)
    )

  def organisationMapper(requestDonation: RequestDonation, accountName: String, config: Config)(
    org: Organisation
  ): (Organisation, Either[DonationError, (Organisation, Donor)]) =
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

    org.donate(donor, donationPart, donation, accountName, config) match
      case Left(error)   => (org, Left(error))
      case Right(newOrg) => (newOrg, Right((newOrg, donor)))

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

  private[api] case class DonorResponse(
    id: String,
    name: String,
    email: String
  )

  // Define the Donation case class to represent each donation
  private[api] case class DonationResponse(
    donationId: String,
    donor: DonorResponse,
    amount: Money,
    organisation: String,
    date: LocalDateTime,
    earmarking: Option[String],
    status: Seq[StatusResponse]
  )

  private[api] case class DonationListResponse(
    donations: Seq[DonationResponse]
  )
