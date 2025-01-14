package com.just.donate.security

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.*
import org.http4s.dsl.io.{/, *}
import org.typelevel.vault.Key
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import java.time.Instant
import scala.util.{Failure, Success}

object AuthMiddleware:

  private val secretKey = sys.env.getOrElse("JWT_SECRET_KEY", "HAu/gwjy5124uMaX9wTAEPPXYDwsCYIWeZ7JnpRTRRk=")
  private val algorithm = JwtAlgorithm.HS256

  def apply(protectedRoutes: HttpRoutes[IO]): HttpRoutes[IO] = Kleisli { req =>
    OptionT {
      // Enable themes to be loaded without authentication
      req.cookies.find(_.name == "jwtToken") match
        case Some(cookie) =>
          validateJwt(cookie.content) match
            case Right(claims) =>
              // Add claims to the request's Vault and call the protected routes
              val updatedReq = req.withAttribute(AuthAttributes.UserClaims, claims)
              protectedRoutes(updatedReq).value
            case Left(error) =>
              // Invalid token
              Forbidden(s"Invalid token: $error").map(Some(_))
        case None =>
          // Missing cookie
          Forbidden("Missing authentication cookie").map(Some(_))
    }
  }

  def validateJwt(token: String): Either[String, JwtClaim] =
    Jwt.decode(token, secretKey, Seq(algorithm)) match
      case Success(claim) =>
        if isExpired(claim) then Left("Token expired")
        else Right(claim)
      case Failure(exception) => Left(exception.getMessage)

  private def isExpired(claim: JwtClaim): Boolean =
    claim.expiration match
      case Some(exp) => {
        exp < Instant.now().getEpochSecond
      }
      case None => true

private object AuthAttributes:
  val UserClaims: Key[JwtClaim] = Key.newKey[IO, JwtClaim].unsafeRunSync()
