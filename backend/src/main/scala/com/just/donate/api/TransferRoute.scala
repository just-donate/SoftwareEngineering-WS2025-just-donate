package com.just.donate.api

import cats.effect.*
import com.just.donate.store.FileStore
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object TransferRoute:

  val transferRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:

    case req@POST -> Root / "organisation" / organisationId / "transfer" => for
      transfer <- req.as[RequestTransfer]
      organisation <- FileStore.load(organisationId)
      response <- organisation match
        case Some(organisation) => ??? // TODO: Implement transfer
        case None => NotFound()
    yield response

  private case class RequestTransfer(fromAccount: String, toAccount: String, amount: BigDecimal, earmarking: Option[String])