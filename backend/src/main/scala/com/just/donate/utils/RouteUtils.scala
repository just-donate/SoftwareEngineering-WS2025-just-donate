package com.just.donate.utils

import cats.effect.IO
import com.just.donate.models.Organisation
import com.just.donate.store.Store
import io.circe.Encoder
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object RouteUtils:

  inline def loadOrganisation[R: Encoder](
    organisationId: String
  )(store: Store)(mapper: Organisation => R): IO[Response[IO]] = for
    organisation <- store.load(organisationId)
    response <- organisation match
      case Some(organisation) => Ok(mapper(organisation))
      case None               => NotFound()
  yield response

  inline def loadAndSaveOrganisation(
    organisationId: String
  )(store: Store)(mapper: Organisation => Organisation): IO[Response[IO]] = for
    organisation <- store.load(organisationId)
    response <- organisation match
      case Some(organisation) =>
        store.save(organisationId, mapper(organisation)) >> Ok()
      case None => NotFound()
  yield response

  inline def loadAndSaveOrganisationOps[R](
    organisationId: String
  )(store: Store)(mapper: Organisation => (Organisation, R)): IO[Option[R]] = for
    organisation <- store.load(organisationId)
    response <- organisation match
      case Some(organisation) =>
        val (newOrganisation, response) = mapper(organisation)
        store.save(organisationId, newOrganisation) >> IO.pure(Some(response))
      case None => IO.pure(None)
  yield response