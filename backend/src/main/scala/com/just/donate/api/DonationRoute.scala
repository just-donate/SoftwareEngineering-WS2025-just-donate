package com.just.donate.api

import cats.effect.*
import com.just.donate.store.FileStore
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object DonationRoute:

  val donationRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:

    case req@POST -> Root / "organisation" / organisationId / "account" / accountName / "donate" => for
      donation <- req.as[RequestDonation]
      organisation <- FileStore.load(organisationId)
      response <- organisation match
        case Some(organisation) =>
          FileStore.save(organisationId, organisation.donate(donation.donor, donation.amount, donation.earmarking, accountName)) >> Ok()
        case None => NotFound()
    yield response

  private case class RequestDonation(donor: String, amount: BigDecimal, earmarking: Option[String])
    
    
    