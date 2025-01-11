package com.just.donate.api

import cats.effect.*
import cats.syntax.all.*
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.models.errors.WithdrawError
import com.just.donate.notify.IEmailService
import com.just.donate.utils.Money
import com.just.donate.utils.RouteUtils.loadAndSaveOrganisationOps
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object TransferRoute:

  val transferRoute: (Repository[String, Organisation], Config, IEmailService) => HttpRoutes[IO] =
    (repository, config, emailService) =>
      HttpRoutes.of[IO]:

        case req @ POST -> Root / organisationId =>
          (for
            transfer <- req.as[RequestTransfer]
            emailMessages <- loadAndSaveOrganisationOps(organisationId)(repository)(org =>
              org.transfer(transfer.amount, transfer.fromAccount, transfer.toAccount, config) match
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

  private[api] case class RequestTransfer(
    fromAccount: String,
    toAccount: String,
    amount: Money
  )
