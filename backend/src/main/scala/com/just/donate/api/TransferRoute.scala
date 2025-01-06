package com.just.donate.api

import cats.effect.*
import cats.syntax.all.*
import com.just.donate.config.Config
import com.just.donate.notify.IEmailService
import com.just.donate.store.Store
import com.just.donate.utils.RouteUtils.loadAndSaveOrganisationOps
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*
import com.just.donate.models.{ Organisation, TransferError }

object TransferRoute:

  val transferRoute: (Store, Config, IEmailService) => HttpRoutes[IO] = (store, config, emailService) =>
    HttpRoutes.of[IO]:

      case req @ POST -> Root / organisationId / "transfer" =>
        (for
          transfer <- req.as[RequestTransfer]
          emailMessages <- loadAndSaveOrganisationOps(organisationId)(store)(org =>
            org.transfer(transfer.amount, transfer.fromAccount, transfer.toAccount, config) match
              case Left(error)                    => (org, Left(error))
              case Right((newOrg, emailMessages)) => (newOrg, Right(emailMessages))
          )
          response <- emailMessages match
            case None                                      => BadRequest("Organisation not found")
            case Some(Left(TransferError.INVALID_ACCOUNT)) => BadRequest(s"Account not found")
            case Some(Left(TransferError.INSUFFICIENT_ACCOUNT_FUNDS)) =>
              BadRequest(s"Source account has insufficient funds")
            case Some(Left(TransferError.NON_POSITIVE_AMOUNT)) => BadRequest("Amount has to be positive")
            case Some(Left(TransferError.SAME_SOURCE_AND_DESTINATION_ACCOUNT)) =>
              BadRequest("The source and target accounts are the same")
            case Some(Left(TransferError.INVALID_DONOR)) => BadRequest(s"Donor not found")
            case Some(Right(emailMessages)) =>
              emailMessages
                .map(message => emailService.sendEmail(message.targetAddress, message.message, message.subject))
                .sequence >> Ok()
        yield response).handleErrorWith {
          case e: InvalidMessageBodyFailure => BadRequest(e.getMessage)
        }

  private case class RequestTransfer(
    fromAccount: String,
    toAccount: String,
    amount: BigDecimal
  )
