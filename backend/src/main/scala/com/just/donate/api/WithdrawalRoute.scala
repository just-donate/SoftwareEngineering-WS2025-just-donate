package com.just.donate.api

import cats.effect.*
import cats.implicits.*
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

object WithdrawalRoute:

  val withdrawalRoute: (Store, Config, IEmailService) => HttpRoutes[IO] = (store, config, emailService) =>
    HttpRoutes.of[IO]:

      case req @ POST -> Root / organisationId / "account" / accountName / "withdrawal" =>
        for
          donation <- req.as[RequestWithdrawal]
          emailMessages <- loadAndSaveOrganisationOps(organisationId)(store)(org =>
            org.withdrawal(donation.amount, accountName, donation.description, donation.earmarking, config) match
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
        yield response

  private case class RequestWithdrawal(amount: BigDecimal, description: String, earmarking: Option[String])
