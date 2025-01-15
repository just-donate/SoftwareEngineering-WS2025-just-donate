package com.just.donate.api.helper

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.{Request, Response, Status}

object ApiRun:

  def apiRun[R](
    httpRoute: Kleisli[IO, Request[IO], Response[IO]],
    actions: Seq[ApiAction[_, _]],
    retrieve: Option[ApiAction[?, R]]
  ): IO[(Status, Option[R])] =
    actions.foldLeft(IO.pure(())) { (acc, action) =>
      acc.flatMap { _ =>
        action.run(httpRoute).void
      }
    }.flatMap { r =>
      retrieve match
        case Some(action) => action.run(httpRoute)
        case None         => IO.pure((Status.Ok, None))
    }
  
  def apiRun[R](
    httpRoute: Kleisli[IO, Request[IO], Response[IO]],
    actions: Seq[ApiAction[_, _]],
    retrieve: ApiAction[?, R]
  ): IO[(Status, Option[R])] =
    apiRun(httpRoute, actions, Some(retrieve))
      
  def apiRun(
    httpRoute: Kleisli[IO, Request[IO], Response[IO]],
    actions: Seq[ApiAction[_, _]]
  ): IO[(Status, Option[Unit])] =
    apiRun(httpRoute, actions, None)