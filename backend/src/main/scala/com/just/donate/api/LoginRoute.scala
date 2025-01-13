package com.just.donate.api

import cats.effect.IO
import com.just.donate.config.AppEnvironment.PRODUCTION
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.user.User
import com.just.donate.utils.CryptoUtils
import io.circe.generic.auto.*
import org.http4s.SameSite.Strict
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.*
import pdi.jwt.{Jwt, JwtAlgorithm}

import java.time.Instant

object LoginRoute:

  private val secretKey = sys.env.getOrElse("JWT_SECRET_KEY", "HAu/gwjy5124uMaX9wTAEPPXYDwsCYIWeZ7JnpRTRRk=")
  private val algorithm = JwtAlgorithm.HS256

  def loginRoute: (Config, Repository[String, User]) => HttpRoutes[IO] =
    (appConfig: Config, userRepo: Repository[String, User]) =>
      HttpRoutes.of[IO]:

        case req @ POST -> Root =>
          (for
            login <- req.as[LoginRequest]
            response <- authenticate(login, appConfig, userRepo)
          yield response).handleErrorWith { _ =>
            Unauthorized(`WWW-Authenticate`(Challenge("Basic", "Malformed request body")))
          }

  private def authenticate(
    login: LoginRequest,
    appConfig: Config,
    userRepo: Repository[String, User]
  ): IO[Response[IO]] =
    userRepo.findById(login.username).flatMap {
      case Some(user) =>
        // Verify that the user is active and the password matches
        if user.active && CryptoUtils.verifyPassword(login.password, user.password) then
          // Authentication successful
          val expirationTimeInSeconds = 3600 // 1 hour in seconds
          val currentTime = Instant.now().getEpochSecond
          val expirationTime = currentTime + expirationTimeInSeconds

          val httpDate = HttpDate
            .fromEpochSecond(expirationTime)
            .getOrElse(
              throw new RuntimeException("Invalid expiration time")
            )

          val claim =
            s"""{
                "username": "${login.username}",
                "orgId": "${login.orgId}",
                "exp": $expirationTime
              }"""

          val token = Jwt.encode(claim, secretKey, algorithm)
          val jwtCookie = ResponseCookie(
            name = "jwtToken",
            content = token,
            httpOnly = true,
            secure = appConfig.environment == PRODUCTION,
            path = Some("/"),
            sameSite = Some(Strict),
            maxAge = Some(expirationTimeInSeconds),
            expires = Some(httpDate)
          )
          Ok(LoginResponse("Login successful")).map(_.addCookie(jwtCookie))
        else Forbidden("Invalid credentials")

      case None =>
        // User not found; respond with forbidden
        Forbidden("Invalid credentials")
    }

  private case class LoginRequest(username: String, password: String, orgId: String)

  private case class LoginResponse(message: String)
