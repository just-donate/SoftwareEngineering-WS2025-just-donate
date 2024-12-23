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

object TransferRoute:

  val transferRoute: Store => HttpRoutes[IO] = (store: Store) =>
    HttpRoutes.of[IO]:

      case req @ POST -> Root / "organisation" / organisationId / "transfer" =>
        for
          transfer <- req.as[RequestTransfer]
          response <- loadAndSaveOrganisation(organisationId)(store)(
            _.transfer(transfer.amount, transfer.fromAccount, transfer.toAccount)
          )
        yield response

  private case class RequestTransfer(
    fromAccount: String,
    toAccount: String,
    amount: BigDecimal
  )
