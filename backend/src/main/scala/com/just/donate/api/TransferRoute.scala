package com.just.donate.api

import cats.effect.*
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
        for
          transfer <- req.as[RequestTransfer]
          transferResult <- loadAndSaveOrganisationOps(organisationId)(store)(org =>
            org.transfer(transfer.amount, transfer.fromAccount, transfer.toAccount, config, emailService) match
              case Left(error)   => (org, Some(error))
              case Right(newOrg) => (newOrg, None)
          )
          response <- transferResult match
            case None                                      => BadRequest("Organisation not found")
            case Some(None)                                => Ok()
            case Some(Some(TransferError.INVALID_ACCOUNT)) => BadRequest(s"Account not found")
            case Some(Some(TransferError.INSUFFICIENT_ACCOUNT_FUNDS)) =>
              BadRequest(s"Source account has insufficient funds")
            case Some(Some(TransferError.NON_POSITIVE_AMOUNT)) => BadRequest("Amount has to be positive")
            case Some(Some(TransferError.SAME_SOURCE_AND_DESTINATION_ACCOUNT)) =>
              BadRequest("The source and target accounts are the same")
            case Some(Some(TransferError.INVALID_DONOR)) => BadRequest(s"Donor not found")
        yield response

  private case class RequestTransfer(
    fromAccount: String,
    toAccount: String,
    amount: BigDecimal
  )

