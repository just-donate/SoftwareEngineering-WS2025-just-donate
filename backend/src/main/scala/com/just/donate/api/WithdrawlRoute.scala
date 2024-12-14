package com.just.donate.api

import cats.effect.*
import com.just.donate.store.FileStore
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object WithdrawlRoute:

  val withdrawlRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:

    case req @ POST -> Root / "organisation" / organisationId / "account" / accountName / "withdrawal" =>
      for
        donation <- req.as[RequestWithdrawl]
        organisation <- FileStore.load(organisationId)
        response <- organisation match
          case Some(organisation) => ??? // TODO: Implement withdrawal
          case None               => NotFound()
      yield response

  private case class RequestWithdrawl(amount: BigDecimal, earmarking: Option[String], description: Option[String])
