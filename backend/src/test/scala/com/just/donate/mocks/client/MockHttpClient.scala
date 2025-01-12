package com.just.donate.mocks.client

import cats.data.Kleisli
import cats.effect.{IO, Resource}
import org.http4s.*
import org.http4s.client.Client

object MockHttpClient {
  def resource(responseHandler: PartialFunction[Request[IO], Resource[IO, Response[IO]]]): Resource[IO, Client[IO]] =
    Resource.pure {
      Client[IO](req =>
        responseHandler.applyOrElse(req, (_: Request[IO]) => Resource.pure(Response[IO](Status.NotFound)))
      )
    }
}
