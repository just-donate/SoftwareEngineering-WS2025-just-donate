package com.just.donate.api

import cats.effect.IO
import com.just.donate.config.AppEnvironment.PRODUCTION
import com.just.donate.config.Config
import org.http4s.{HttpRoutes, *}
import org.http4s.Method.POST
import org.http4s.SameSite.Strict
import org.http4s.dsl.io.*

object LogoutRoute:

  val logoutRoute: Config => HttpRoutes[IO] = (appConfig: Config) =>
    HttpRoutes.of[IO]:
      case req @ POST -> Root =>
        val expiredCookie = ResponseCookie(
          name = "jwtToken",
          content = "",
          httpOnly = true,
          secure = appConfig.environment == PRODUCTION, // Use secure = false in development
          path = Some("/"),
          sameSite = Some(Strict),
          maxAge = Some(0) // Expire the cookie immediately
        )
        Ok("Logout successful").map(_.removeCookie(expiredCookie.name))
