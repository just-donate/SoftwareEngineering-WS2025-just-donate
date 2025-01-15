package com.just.donate.security

import cats.effect.IO
import cats.syntax.all
import munit.CatsEffectSuite
import org.http4s
import org.http4s.dsl.io
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.uri
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import java.time.Instant

class AuthMiddlewareSuite extends CatsEffectSuite {

  // Use the same secret key/algorithm as in AuthMiddleware
  private val secretKey = sys.env.getOrElse("JWT_SECRET_KEY", "HAu/gwjy5124uMaX9wTAEPPXYDwsCYIWeZ7JnpRTRRk=")
  private val algorithm = JwtAlgorithm.HS256

  // A helper route that requires authentication
  // If successful, it responds with "Protected route success"
  private val baseProtectedRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "protected" =>
      Ok("Protected route success")
  }

  // Wrap the base route with AuthMiddleware
  private val protectedRoute: HttpRoutes[IO] = AuthMiddleware(baseProtectedRoute)

  // Helper to generate a valid JWT token, optionally specifying
  // expiration and other claim details
  private def generateToken(
                             subject: String = "TestUser",
                             validForSeconds: Long = 3600,
                             withExpiration: Boolean = true,
                             issuedAt: Instant = Instant.now
                           ): String = {
    val nowEpoch    = issuedAt.getEpochSecond
    val expEpochOpt = if (withExpiration) Some(nowEpoch + validForSeconds) else None

    val claim = JwtClaim(
      subject   = Some(subject),
      issuedAt  = Some(nowEpoch),
      expiration= expEpochOpt
    )

    Jwt.encode(claim, secretKey, algorithm)
  }

  // ---------------------------------------------------------------
  // 1. No cookie, no Authorization header => 403 Forbidden
  // ---------------------------------------------------------------
  test("No cookie, no Authorization header => 403 Forbidden with 'No authentication token provided'") {
    val req = Request[IO](Method.GET, uri"/protected")

    protectedRoute.run(req).value.flatMap {
      case Some(resp) =>
        assertEquals(resp.status, Status.Forbidden)
        resp.as[String].map { body =>
          assert(body.contains("No authentication token provided"))
        }
      case None =>
        fail("Expected a response; got None")
    }
  }

  // ---------------------------------------------------------------
  // 2. Cookie with valid token => 200 OK
  // ---------------------------------------------------------------
  test("Cookie with valid token => 200 OK with 'Protected route success'") {
    val validToken = generateToken()
    // Add a jwtToken cookie
    val cookie = RequestCookie("jwtToken", validToken)
    val req    = Request[IO](Method.GET, uri"/protected").addCookie(cookie)

    protectedRoute.run(req).value.flatMap {
      case Some(resp) =>
        assertEquals(resp.status, Status.Ok)
        resp.as[String].map { body =>
          assertEquals(body, "Protected route success")
        }
      case None =>
        fail("Expected a response; got None")
    }
  }

  // ---------------------------------------------------------------
  // 3. Cookie with invalid token => 403 Forbidden ("Invalid token:")
  // ---------------------------------------------------------------
  test("Cookie with invalid token => 403 Forbidden (Invalid token)") {
    // For a quick invalid token, just take a valid token and remove last char
    val almostValidToken = generateToken().dropRight(1)
    val cookie = RequestCookie("jwtToken", almostValidToken)
    val req    = Request[IO](Method.GET, uri"/protected").addCookie(cookie)

    protectedRoute.run(req).value.flatMap {
      case Some(resp) =>
        assertEquals(resp.status, Status.Forbidden)
        resp.as[String].map { body =>
          assert(body.contains("Invalid token"))
        }
      case None =>
        fail("Expected a response; got None")
    }
  }

  // ---------------------------------------------------------------
  // 4. Cookie with expired token => 403 Forbidden ("Token expired")
  // ---------------------------------------------------------------
  test("Cookie with expired token => 403 Forbidden (Token expired)") {
    // Generate a token that expired 5 seconds ago
    val expiredToken = generateToken(validForSeconds = -5)
    val cookie = RequestCookie("jwtToken", expiredToken)
    val req    = Request[IO](Method.GET, uri"/protected").addCookie(cookie)

    protectedRoute.run(req).value.flatMap {
      case Some(resp) =>
        assertEquals(resp.status, Status.Forbidden)
        resp.as[String].map { body =>
          assert(body.contains("Invalid token: The token is expired since"))
        }
      case None =>
        fail("Expected a response; got None")
    }
  }

  // ---------------------------------------------------------------
  // 5. Cookie with no expiration => 403 Forbidden (Token expired)
  // ---------------------------------------------------------------
  test("Cookie with no expiration => 403 Forbidden (Token expired)") {
    // Generate a token but do not set expiration
    val noExpToken = generateToken(withExpiration = false)
    val cookie = RequestCookie("jwtToken", noExpToken)
    val req    = Request[IO](Method.GET, uri"/protected").addCookie(cookie)

    protectedRoute.run(req).value.flatMap {
      case Some(resp) =>
        assertEquals(resp.status, Status.Forbidden)
        resp.as[String].map { body =>
          assert(body.contains("Token expired"))
        }
      case None =>
        fail("Expected a response; got None")
    }
  }

  // ---------------------------------------------------------------
  // 6. Authorization header with valid "Bearer <token>" => 200 OK
  // ---------------------------------------------------------------
  test("Authorization header with valid Bearer token => 200 OK") {
    val validToken = generateToken()
    val authHeader = Header.Raw(org.http4s.headers.Authorization.name, s"Bearer $validToken")

    val req = Request[IO](Method.GET, uri"/protected")
      .withHeaders(Headers(authHeader))

    protectedRoute.run(req).value.flatMap {
      case Some(resp) =>
        assertEquals(resp.status, Status.Ok)
        resp.as[String].map { body =>
          assertEquals(body, "Protected route success")
        }
      case None =>
        fail("Expected a response; got None")
    }
  }

  // ---------------------------------------------------------------
  // 7. Authorization header with invalid token => 403 Forbidden
  // ---------------------------------------------------------------
  test("Authorization header with invalid token => 403 Forbidden") {
    val invalidToken = generateToken().dropRight(2) // break signature
    val authHeader = Header.Raw(org.http4s.headers.Authorization.name, s"Bearer $invalidToken")

    val req = Request[IO](Method.GET, uri"/protected")
      .withHeaders(Headers(authHeader))

    protectedRoute.run(req).value.flatMap {
      case Some(resp) =>
        assertEquals(resp.status, Status.Forbidden)
        resp.as[String].map { body =>
          assert(body.contains("Invalid token"))
        }
      case None =>
        fail("Expected a response; got None")
    }
  }

  // ---------------------------------------------------------------
  // 8. Authorization header not starting with "Bearer "
  // ---------------------------------------------------------------
  test("""Authorization header not "Bearer " => 403 Forbidden (No valid Bearer token found)""") {
    // We'll prefix with "Token " instead
    val validToken = generateToken()
    val authHeader = Header.Raw(org.http4s.headers.Authorization.name, s"Token $validToken")

    val req = Request[IO](Method.GET, uri"/protected")
      .withHeaders(Headers(authHeader))

    protectedRoute.run(req).value.flatMap {
      case Some(resp) =>
        assertEquals(resp.status, Status.Forbidden)
        resp.as[String].map { body =>
          assert(body.contains("No valid Bearer token found"))
        }
      case None =>
        fail("Expected a response; got None")
    }
  }

}

