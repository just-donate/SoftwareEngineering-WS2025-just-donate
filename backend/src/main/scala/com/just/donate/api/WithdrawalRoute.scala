package com.just.donate.api

import cats.effect.*
import cats.implicits.*
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.notify.IEmailService
import com.just.donate.utils.Money
import com.just.donate.utils.RouteUtils.loadAndSaveOrganisationOps
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object WithdrawalRoute:

  val withdrawalRoute: (Repository[String, Organisation], Config, IEmailService) => HttpRoutes[IO] =
    (repository, config, emailService) =>
      HttpRoutes.of[IO]:

        case req @ POST -> Root / organisationId =>
          (for
            donation <- req.as[RequestWithdrawal]
            emailMessages <- loadAndSaveOrganisationOps(organisationId)(repository)(org =>
              org.withdrawal(
                donation.amount,
                donation.fromAccount,
                donation.description,
                donation.earmarking.flatMap(org.getEarmarking),
                config
              ) match
                case Left(error)                    => (org, Left(error))
                case Right((newOrg, emailMessages)) => (newOrg, Right(emailMessages))
            )
            response <- emailMessages match
              case None                      => BadRequest("Organisation not found")
              case Some(Left(transferError)) => BadRequest(transferError.message)
              case Some(Right(emailMessages)) =>
                emailMessages
                  .map(message => emailService.sendEmail(message.targetAddress, message.message, message.subject))
                  .sequence >> Ok()
          yield response).handleErrorWith {
            case e: InvalidMessageBodyFailure => BadRequest(e.getMessage)
          }

  private[api] case class RequestWithdrawal(
    fromAccount: String,
    amount: Money,
    description: String,
    earmarking: Option[String]
  )
