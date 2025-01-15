package com.just.donate.api.helper

import cats.data.Kleisli
import cats.effect.IO
import com.just.donate.api.OrganisationRoute.{RequestAccount, RequestEarmarking, ResponseAccount, ResponseEarmarking}
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
  ct: ClassTag[B]
):
  def run(httpRoute: Kleisli[IO, Request[IO], Response[IO]]): IO[(Status, Option[R])] =
    val req =
      if ct.runtimeClass == classOf[Unit] then Request[IO](method, Uri.unsafeFromString(uri.mkString("/")))
      else Request[IO](method, Uri.unsafeFromString(uri.mkString("/"))).withEntity(body)

    for
      resp <- httpRoute.run(req)
      status = resp.status
      response <- ct.runtimeClass match
        case c if c == classOf[Unit]  => IO.pure(None)
        case _ if status != Status.Ok => IO.pure(None)
        case _                        => resp.as[R].map(Some(_))
    yield (status, response)

object ApiAction:

  case class AddEarmarking(name: String, description: String)
      extends ApiAction[RequestEarmarking, Unit](
        Method.POST,
        Seq("earmarking"),
        RequestEarmarking(name, description)
      )

  case class ListEarmarkings()
      extends ApiAction[Unit, Seq[ResponseEarmarking]](
        Method.GET,
        Seq("earmarking", "list"),
        ()
      )

  case class AddBankAccount(name: String, initialBalance: Money)
      extends ApiAction[RequestAccount, Unit](
        Method.POST,
        Seq("account"),
        RequestAccount(name, initialBalance)
      )

  case class ListBankAccounts()
      extends ApiAction[Unit, Seq[ResponseAccount]](
        Method.GET,
        Seq("account", "list"),
        ()
      )
