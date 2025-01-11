package com.just.donate.utils

import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import io.circe.Encoder
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object RouteUtils:

  inline def loadOrganisation[R: Encoder](
    organisationId: String
  )(repository: Repository[String, Organisation])(mapper: Organisation => R): IO[Response[IO]] = for
    organisation <- repository.findById(organisationId)
    response <- organisation match
      case Some(organisation) => Ok(mapper(organisation))
      case None               => NotFound()
  yield response

  inline def loadAndSaveOrganisation(
    organisationId: String
  )(repository: Repository[String, Organisation])(mapper: Organisation => Organisation): IO[Response[IO]] = for
    organisation <- repository.findById(organisationId)
    response <- organisation match
      case Some(organisation) =>
        repository.save(mapper(organisation)) >> Ok()
      case None => NotFound()
  yield response

  inline def loadAndSaveOrganisationOps[R](
    organisationId: String
  )(repository: Repository[String, Organisation])(mapper: Organisation => (Organisation, R)): IO[Option[R]] = for
    organisation <- repository.findById(organisationId)
    response <- organisation match
      case Some(organisation) =>
        val (newOrganisation, response) = mapper(organisation)
        repository.save(newOrganisation) >> IO.pure(Some(response))
      case None => IO.pure(None)
  yield response
