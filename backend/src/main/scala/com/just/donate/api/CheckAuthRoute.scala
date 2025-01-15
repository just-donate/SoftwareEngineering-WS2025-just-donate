package com.just.donate.api

import cats.effect.IO
import com.just.donate.security.AuthMiddleware.validateJwt
import org.http4s.{Header, HttpRoutes}
import org.http4s.dsl.io.*
import pdi.jwt.JwtClaim

object CheckAuthRoute {

  val checkAuthRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root =>
      // Check for the token in the cookie
      req.cookies.find(_.name == "jwtToken") match {
        case Some(cookie) =>
          validateJwt(cookie.content) match {
            case Right(_) => Ok("""{"message": "Authenticated"}""")
            case Left(_)  => Forbidden("""{"error": "Invalid or tempered token"}""")
          }
        case None =>
          // Fallback to check for the Authorization header
          req.headers.get(org.http4s.headers.Authorization.name) match {
            case Some(nonEmptyHeaders) =>
              // Extract the Bearer token from the NonEmptyList
              nonEmptyHeaders.collectFirst {
                case Header.Raw(name, value) if name.toString.equalsIgnoreCase("Authorization") && value.startsWith("Bearer ") =>
                  value.stripPrefix("Bearer ")
              } match {
                case Some(token) =>
                  validateJwt(token) match {
                    case Right(_) => Ok("""{"message": "Authenticated"}""")
                    case Left(_)  => Forbidden("""{"error": "Invalid or tempered token"}""")
                  }
                case None =>
                  Forbidden("""{"error": "No valid Bearer token found in Authorization header"}""")
              }
            case None =>
              Forbidden("""{"error": "No authentication token provided"}""")
          }
      }
  }
}
