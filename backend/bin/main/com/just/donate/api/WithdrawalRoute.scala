package com.just.donate.api

import cats.effect.*
import com.just.donate.store.Store
import com.just.donate.utils.RouteUtils.loadAndSaveOrganisation
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object WithdrawalRoute:

  val withdrawalRoute: Store => HttpRoutes[IO] = (store: Store) =>
    HttpRoutes.of[IO]:

      case req @ POST -> Root / organisationId / "account" / accountName / "withdrawal" =>
        for
          donation <- req.as[RequestWithdrawal]
          response <- loadAndSaveOrganisation(organisationId)(store)(
            _.withdrawal(donation.amount, accountName, donation.earmarking)
          )
        yield response

  private case class RequestWithdrawal(amount: BigDecimal, earmarking: Option[String], description: Option[String])
