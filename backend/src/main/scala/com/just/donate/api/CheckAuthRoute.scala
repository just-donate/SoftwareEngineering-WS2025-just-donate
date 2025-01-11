package com.just.donate.api

import cats.effect.IO
import com.just.donate.security.AuthMiddleware.validateJwt
import org.http4s.dsl.io.*
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{Challenge, HttpRoutes}
import pdi.jwt.{JwtAlgorithm, JwtClaim}

object CheckAuthRoute {

  private val secretKey = sys.env.getOrElse("JWT_SECRET_KEY", "your-secret-key")
  private val algorithm = JwtAlgorithm.HS256

  val checkAuthRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root / "check-auth" =>
      req.cookies.find(_.name == "jwtToken") match {
        case Some(cookie) =>
          validateJwt(cookie.content) match {
            case Right(_) =>
              Ok("""{"message": "Authenticated"}""")
            case Left(error) =>
              Unauthorized(`WWW-Authenticate`(Challenge("Basic", "Tempered token")))
          }
        case None =>
          Unauthorized(`WWW-Authenticate`(Challenge("Basic", "No cookie found")))
      }
  }
}

