package com.just.donate.api.public

import cats.effect.*
import com.just.donate.db.Repository
import com.just.donate.models.{Organisation, ThemeConfig}
import com.just.donate.utils.Money
import com.just.donate.utils.RouteUtils.loadOrganisation
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io.*

object OrganizationPublicRoute:

  val publicApi: Repository[String, Organisation] => HttpRoutes[IO] = repository =>
    HttpRoutes.of[IO]:

      case GET -> Root / organisationId / "earmarking" / "list" =>
        loadOrganisation(organisationId)(repository)(
          _.getEarmarkings.map(e => ResponseEarmarking(e.name, e.description)).toSeq
        ).onError(error => IO.println("Error getting earmarkings: " + error))

      case GET -> Root / organisationId / "theme" =>
        for
          organisation <- repository.findById(organisationId)
          response <- organisation match
            case Some(org) =>
              org.theme match
                case Some(value) => Ok(value)
                case None        => NotFound()
            case None => NotFound()
        yield response

  case class RequestOrganisation(name: String)

  private[api] case class ResponseOrganisation(organisationId: String, name: String)

  private[api] case class RequestEarmarking(name: String, description: String)

  private[api] case class ResponseEarmarking(name: String, description: String)

  private[api] case class RequestAccount(name: String, balance: Money)

  private[api] case class ResponseAccount(name: String, balance: Money)

  private[api] case class RequestDonation(donor: String, amount: Money, earmarking: Option[String])

