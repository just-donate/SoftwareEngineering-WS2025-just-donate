package com.just.donate.api

import cats.effect.IO
import com.just.donate.security.AuthMiddleware.validateJwt
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import pdi.jwt.JwtClaim

object CheckAuthRoute {

  val checkAuthRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root =>
      req.cookies.find(_.name == "jwtToken") match {
        case Some(cookie) =>
          validateJwt(cookie.content) match {
            case Right(_) =>
              Ok("""{"message": "Authenticated"}""")
            case Left(error) =>
              Forbidden(s"Forbidden: Tempered Token")
          }
        case None =>
          Forbidden(s"No cookie found")
      }
  }
}

