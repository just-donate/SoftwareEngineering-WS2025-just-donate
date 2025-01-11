package com.just.donate.api

import cats.effect.IO
import com.just.donate.config.{AppConfig, Config}
import com.just.donate.config.AppEnvironment.PRODUCTION
import io.circe.generic.auto.*
import org.http4s.SameSite.Strict
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{Challenge, HttpRoutes, Response, ResponseCookie}
import pdi.jwt.{Jwt, JwtAlgorithm}

import java.time.Instant

object LoginRoute {

  private val secretKey = sys.env.getOrElse("JWT_SECRET_KEY", "HAu/gwjy5124uMaX9wTAEPPXYDwsCYIWeZ7JnpRTRRk=")
  private val algorithm = JwtAlgorithm.HS256

  def loginRoute: Config => HttpRoutes[IO] = (appConfig: Config) =>
    HttpRoutes.of[IO]:

      case req @ POST -> Root =>
        (for {
          login <- req.as[LoginRequest]
          response <- authenticate(login, appConfig)
        } yield response).handleErrorWith { _ =>
          Unauthorized(`WWW-Authenticate`(Challenge("Basic", "Malformed request body")))
        }

  private def authenticate(login: LoginRequest, appConfig: Config): IO[Response[IO]] = {
    // TODO: Implement actual authentication logic
    if (login.username == "admin@gmail.com" && login.password == "admin") {
      val expirationTimeInSeconds = 3600 // 1 hour in seconds
      val currentTime = Instant.now().getEpochSecond
      val expirationTime = currentTime + expirationTimeInSeconds

      val claim =
        s"""{
        "username": "${login.username}",
        "orgId": "${login.orgId}",
        "expiration": $expirationTime
      }"""
      val token = Jwt.encode(claim, secretKey, algorithm)
      val jwtCookie = ResponseCookie(
        name = "jwtToken",
        content = token,
        httpOnly = true,
        secure = appConfig.environment == PRODUCTION,
        path = Some("/"),
        sameSite = Some(Strict),
        maxAge = Some(3600)
      )
      Ok(LoginResponse("Login successful")).map(_.addCookie(jwtCookie))
    } else {
      Forbidden("Invalid credentials")
    }
  }

  private case class LoginRequest(username: String, password: String, orgId: String)

  private case class LoginResponse(message: String)
}

