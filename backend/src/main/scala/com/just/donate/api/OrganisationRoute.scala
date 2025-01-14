package com.just.donate.api

import cats.effect.*
import com.just.donate.db.Repository
import com.just.donate.models.{Organisation, ThemeConfig, EarmarkingImage}
import com.just.donate.utils.Money
import com.just.donate.utils.RouteUtils.{loadAndSaveOrganisation, loadOrganisation}
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
          response <- loadAndSaveOrganisation(organisationId)(repository)(_.addEarmarking(earmarking.name))
        yield response

      case req @ POST -> Root / organisationId / "earmarking" / earmarking / "image" =>
        for
          image <- req.as[RequestEarmarkingImage]
          response <- loadAndSaveOrganisation(organisationId)(repository)(_.addEarmarkingImage(earmarking, EarmarkingImage(image.fileUrl)))
        yield response

      case GET -> Root / organisationId / "earmarking" / earmarking / "image" / "list" =>
        // This returns an array of arrays of EarmarkingImage objects
        // We need to flatten the array to get a single array of EarmarkingImage objects
        loadOrganisation(organisationId)(repository)(_.getEarmarkingImages(earmarking).map(images => images.map(ResponseEarmarkingImage(_))).toSeq.flatten)

      case DELETE -> Root / organisationId / "earmarking" / earmarking =>
        loadAndSaveOrganisation(organisationId)(repository)(_.removeEarmarking(earmarking))

      case GET -> Root / organisationId / "earmarking" / "list" =>
        loadOrganisation(organisationId)(repository)(
          _.accounts.headOption.map(
            _._2.boundDonations.map(_._1).map(ResponseEarmarking(_))
          ).getOrElse(Seq())
        )

      case GET -> Root / organisationId / "account" / "list" =>
        loadOrganisation(organisationId)(repository)(_.accounts.map(a => ResponseAccount(a._1, a._2.totalBalance)).toSeq)

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

      case GET -> Root / organisationId / "theme" => for
        organisation <- repository.findById(organisationId)
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

  private[api] case class RequestEarmarkingImage(fileUrl: String)

  private[api] case class ResponseEarmarkingImage(image: EarmarkingImage)

  private[api] case class RequestAccount(name: String, balance: Money)

  private[api] case class ResponseAccount(name: String, balance: Money)

  private[api] case class RequestDonation(donor: String, amount: Money, earmarking: Option[String])
