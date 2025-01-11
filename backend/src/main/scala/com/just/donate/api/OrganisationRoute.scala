package com.just.donate.api

import cats.effect.*
import com.just.donate.models.{Organisation, ThemeConfig}
import com.just.donate.store.Store
import com.just.donate.utils.Money
import com.just.donate.utils.RouteUtils.{loadAndSaveOrganisation, loadOrganisation}
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

import java.nio.file.{Files, Paths, StandardOpenOption}

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

      case req @ POST -> Root / organisationId / "earmarking" =>
        for
          earmarking <- req.as[RequestEarmarking]
          response <- loadAndSaveOrganisation(organisationId)(store)(_.addEarmarking(earmarking.name))
        yield response

      case DELETE -> Root / organisationId / "earmarking" / earmarking =>
        loadAndSaveOrganisation(organisationId)(store)(_.removeEarmarking(earmarking))

      case GET -> Root / organisationId / "earmarking" / "list" =>
        loadOrganisation(organisationId)(store)(
          _.accounts.headOption.map(
            _._2.boundDonations.map(_._1).map(ResponseEarmarking(_))
          ).getOrElse(Seq())
        )

      case GET -> Root / organisationId / "account" / "list" =>
        loadOrganisation(organisationId)(store)(_.accounts.map(a => ResponseAccount(a._1, a._2.totalBalance)).toSeq)

      case req @ POST -> Root / organisationId / "account" =>
        for
          account <- req.as[RequestAccount]
          response <- loadAndSaveOrganisation(organisationId)(store)(_.addAccount(account.name, account.balance))
        yield response

      case DELETE -> Root / organisationId / "account" / accountName =>
        loadAndSaveOrganisation(organisationId)(store)(_.removeAccount(accountName))

      case GET -> Root / organisationId / "transaction" / "list" =>
        Ok(Seq.empty)

      case req @ POST -> Root / organisationId / "theme" =>
        for
          theme <- req.as[ThemeConfig]
          response <- loadAndSaveOrganisation(organisationId)(store)(_.setTheme(theme))
        yield response

      case GET -> Root / organisationId / "theme" => for
        organisation <- store.load(organisationId)
        response <- organisation match
          case Some(org) => org.theme match
            case Some(value) => Ok(value)
            case None => NotFound()
          case None => NotFound()
      yield response

  case class RequestOrganisation(name: String)

  private[api] case class ResponseOrganisation(organisationId: String, name: String)

  private[api] case class RequestEarmarking(name: String)

  private[api] case class ResponseEarmarking(name: String)

  private[api] case class RequestAccount(name: String, balance: Money)

  private[api] case class ResponseAccount(name: String, balance: Money)

  private[api] case class RequestDonation(donor: String, amount: Money, earmarking: Option[String])
