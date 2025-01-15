package com.just.donate.api.helper

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.{Request, Response, Status}

object ApiRun:

  def apiRun[R](
    httpRoute: Kleisli[IO, Request[IO], Response[IO]],
    actions: Seq[ApiAction[_, _]],
    retrieve: ApiAction[?, R]
  ): IO[(Status, Option[R])] =
    actions.foldLeft(IO.pure(())) { (acc, action) =>
      acc.flatMap { _ =>
        action.run(httpRoute).void
      }
    }.flatMap { _ =>
      retrieve.run(httpRoute)
    }
