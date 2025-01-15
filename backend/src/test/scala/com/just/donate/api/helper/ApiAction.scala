package com.just.donate.api.helper

import cats.data.Kleisli
import cats.effect.IO
import com.just.donate.api.OrganisationRoute.*
import com.just.donate.api.DonationRoute.{RequestDonation, DonationListResponse}
import com.just.donate.utils.Money
import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

import scala.reflect.ClassTag

sealed trait ApiAction[B, R](method: Method, uri: Seq[String], body: B)(using
  encoder: Encoder[B],
  decoder: Decoder[R],
  ctB: ClassTag[B],
  ctR: ClassTag[R]
):
  def run(httpRoute: Kleisli[IO, Request[IO], Response[IO]]): IO[(Status, Option[R])] =
    val req =
      if ctB.runtimeClass == classOf[Unit] then Request[IO](method, Uri.unsafeFromString(uri.mkString("/")))
      else Request[IO](method, Uri.unsafeFromString(uri.mkString("/"))).withEntity(body)

    for
      resp <- httpRoute.run(req)
      status = resp.status
      response <- ctR.runtimeClass match
        case c if c == classOf[Unit]  => IO.pure(None)
        case _ if status != Status.Ok => IO.pure(None)
        case _                        => resp.as[R].map(Some(_))
    yield (status, response)

object ApiAction:

  case class AddOrganisation(name: String)
      extends ApiAction[RequestOrganisation, ResponseOrganisation](
        Method.POST,
        Seq(""),
        RequestOrganisation(name)
      )

  case class ListOrganisations()
      extends ApiAction[Unit, Seq[String]](
        Method.GET,
        Seq("list"),
        ()
      )

  case class GetOrganisation(id: String)
      extends ApiAction[Unit, ResponseOrganisation](
        Method.GET,
        Seq(id),
        ()
      )

  case class DeleteOrganisation(id: String)
      extends ApiAction[Unit, Unit](
        Method.DELETE,
        Seq(id),
        ()
      )

  case class AddEarmarking(organisationId: String, name: String, description: String)
      extends ApiAction[RequestEarmarking, Unit](
        Method.POST,
        Seq(organisationId, "earmarking"),
        RequestEarmarking(name, description)
      )

  case class ListEarmarkings(organisationId: String)
      extends ApiAction[Unit, Seq[ResponseEarmarking]](
        Method.GET,
        Seq(organisationId, "earmarking", "list"),
        ()
      )

  case class AddAccount(organisationId: String, name: String, initialBalance: Money)
      extends ApiAction[RequestAccount, Unit](
        Method.POST,
        Seq(organisationId, "account"),
        RequestAccount(name, initialBalance)
      )

  case class ListAccounts(organisationId: String)
      extends ApiAction[Unit, Seq[ResponseAccount]](
        Method.GET,
        Seq(organisationId, "account", "list"),
        ()
      )

  case class GetAccount(organisationId: String, id: String)
      extends ApiAction[Unit, ResponseAccount](
        Method.GET,
        Seq(organisationId, "account", id),
        ()
      )

  case class DeleteAccount(organisationId: String, id: String)
      extends ApiAction[Unit, Unit](
        Method.DELETE,
        Seq(organisationId, "account", id),
        ()
      )

  case class AddDonation(organisationId: String, accountName: String, donorName: String, donorEmail: String, amount: Money, earmarking: Option[String])
      extends ApiAction[RequestDonation, Unit](
        Method.POST,
        Seq(organisationId, "account", accountName),
        RequestDonation(donorName, donorEmail, amount, earmarking)
      )

  case class ListDonations(organisationId: String)
      extends ApiAction[Unit, DonationListResponse](
        Method.GET,
        Seq(organisationId, "donations"),
        ()
      )
