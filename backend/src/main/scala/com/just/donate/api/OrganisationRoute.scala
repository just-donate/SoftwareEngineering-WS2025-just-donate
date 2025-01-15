package com.just.donate.api

import cats.effect.*
import com.just.donate.db.Repository
import com.just.donate.models.{ Earmarking, EarmarkingImage, Organisation, ThemeConfig }
import com.just.donate.utils.Money
import com.just.donate.utils.RouteUtils.{ loadAndSaveOrganisation, loadOrganisation }
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*
import java.time.LocalDateTime

object OrganisationRoute:

  val organisationApi: Repository[String, Organisation] => HttpRoutes[IO] = repository =>
    HttpRoutes.of[IO]:

      case GET -> Root / "list" =>
        Ok(repository.findAll().map(_.map(_.id)))

      case req @ POST -> Root =>
        for
          request <- req.as[RequestOrganisation]
          organisation <- repository.save(Organisation(request.name))
          response <- Ok(ResponseOrganisation(organisation.id, organisation.name))
        yield response

      case GET -> Root / organisationId =>
        loadOrganisation(organisationId)(repository)(o => ResponseOrganisation(organisationId, o.name))

      case DELETE -> Root / organisationId =>
        repository.delete(organisationId) >> Ok()

      case req @ POST -> Root / organisationId / "earmarking" =>
        for
          earmarking <- req.as[RequestEarmarking]
          response <- loadAndSaveOrganisation(organisationId)(repository)(
            _.addEarmarking(Earmarking(earmarking.name, earmarking.description))
          )
        yield response

      case req @ POST -> Root / organisationId / "earmarking" / earmarking / "image" =>
        for
          image <- req.as[RequestEarmarkingImage]
          response <- loadAndSaveOrganisation(organisationId)(repository)(
            _.addEarmarkingImage(earmarking, EarmarkingImage(image.fileUrl))
          )
        yield response

      case DELETE -> Root / organisationId / "earmarking" / earmarking =>
        loadAndSaveOrganisation(organisationId)(repository)(_.removeEarmarking(earmarking))

      case GET -> Root / organisationId / "account" / "list" =>
        loadOrganisation(organisationId)(repository)(org =>
          org.accounts
            .map(a =>
              ResponseAccount(
                a._1,
                a._2.totalBalance,
                org.getEarmarkings
                  .map(e => (e.name, a._2.totalEarmarkedBalance(e)))
                  .toSeq :+ ("Frei", a._2.unboundDonations.totalBalance)
              )
            )
            .toSeq
        )

      case req @ POST -> Root / organisationId / "account" =>
        for
          account <- req.as[RequestAccount]
          response <- loadAndSaveOrganisation(organisationId)(repository)(_.addAccount(account.name, account.balance))
        yield response

      case DELETE -> Root / organisationId / "account" / accountName =>
        loadAndSaveOrganisation(organisationId)(repository)(_.removeAccount(accountName))

      case GET -> Root / organisationId / "transaction" / "list" =>
        Ok(Seq.empty)

      case req @ POST -> Root / organisationId / "theme" =>
        for
          theme <- req.as[ThemeConfig]
          response <- loadAndSaveOrganisation(organisationId)(repository)(_.setTheme(theme))
        yield response

  case class RequestOrganisation(name: String)

  private[api] case class ResponseOrganisation(organisationId: String, name: String)

  private[api] case class RequestEarmarking(name: String, description: String)

  private[api] case class ResponseEarmarking(name: String, description: String)

  private[api] case class RequestEarmarkingImage(fileUrl: String)

  private[api] case class ResponseEarmarkingImage(image: EarmarkingImage)

  private[api] case class RequestAccount(name: String, balance: Money)

  private[api] case class ResponseAccount(name: String, balance: Money, byEarmarking: Seq[(String, Money)])
