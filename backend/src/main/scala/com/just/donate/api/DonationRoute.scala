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

object DonationRoute:

  val donationRoute: Store => HttpRoutes[IO] = (store: Store) =>
    HttpRoutes.of[IO]:

      case req @ POST -> Root / organisationId / "account" / accountName / "donate" =>
        for
          donation <- req.as[RequestDonation]
          response <- loadAndSaveOrganisation(organisationId)(store)(
            _.donate(donation.donor, donation.amount, donation.earmarking, accountName)
          )
        yield response

  private[api] case class RequestDonation(donor: String, amount: BigDecimal, earmarking: Option[String])
