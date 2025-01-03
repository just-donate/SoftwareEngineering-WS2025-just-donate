package com.just.donate.api

import cats.effect.*
import com.just.donate.models.Organisation
import com.just.donate.store.{FileStore, Store}
import com.just.donate.utils.RouteUtils.{loadAndSaveOrganisation, loadOrganisation}
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object OrganisationRoute:
  
  val organisationApi: Store => HttpRoutes[IO] = (store: Store) =>
    HttpRoutes.of[IO]:

      case GET -> Root / "list" =>
        Ok(store.list())

      case req @ POST -> Root =>
        for
          organisation <- req.as[RequestOrganisation]
          organisationId <- IO(organisation.name.hashCode.toString)
          _ <- store.save(organisationId, Organisation(organisation.name))
          response <- Ok(ResponseOrganisation(organisationId, organisation.name))
        yield response

      case GET -> Root / organisationId =>
        loadOrganisation(organisationId)(store)(o => ResponseOrganisation(organisationId, o.name))

      case DELETE -> Root / organisationId =>
        store.delete(organisationId) >> Ok()

      case req @ POST -> Root / organisationId / "account" =>
        for
          account <- req.as[RequestAccount]
          response <- loadAndSaveOrganisation(organisationId)(store)(_.addAccount(account.name))
        yield response

      case DELETE -> Root / organisationId / "account" / accountName =>
        loadAndSaveOrganisation(organisationId)(store)(_.removeAccount(accountName))

      case GET -> Root / organisationId / "account" / "list" =>
        loadOrganisation(organisationId)(store)(_.accounts.map(_.name))
  
  case class RequestOrganisation(name: String)

  private[api] case class ResponseOrganisation(organisationId: String, name: String)

  private[api] case class RequestAccount(name: String, balance: BigDecimal)
