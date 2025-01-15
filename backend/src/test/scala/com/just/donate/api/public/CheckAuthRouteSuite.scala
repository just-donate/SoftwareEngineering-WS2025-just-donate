package com.just.donate.api.public

import cats.effect.IO
import com.just.donate.api.CheckAuthRoute
import com.just.donate.security.AuthMiddleware
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import java.time.Instant
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

class CheckAuthRouteSuite extends CatsEffectSuite {

  // The route under test
  private val route: HttpApp[IO] = CheckAuthRoute.checkAuthRoute.orNotFound

  // Same secret key and algorithm used in AuthMiddleware
  // (So that our "valid token" is recognized by validateJwt)
  private val secretKey = sys.env.getOrElse("JWT_SECRET_KEY", "HAu/gwjy5124uMaX9wTAEPPXYDwsCYIWeZ7JnpRTRRk=")
  private val algorithm = JwtAlgorithm.HS256

  /** Helper to generate a valid JWT with an expiration (1 hour by default). */
  private def generateValidToken(
                                  subject: String = "user@example.com",
                                  validFor: FiniteDuration = 1.hour
                                ): String = {
    val now      = Instant.now.getEpochSecond
    val expires  = now + validFor.toSeconds
    val claims   = JwtClaim(subject = Some(subject), issuedAt = Some(now), expiration = Some(expires))
    Jwt.encode(claims, secretKey, algorithm)
  }

  /** Helper to generate an obviously invalid token. */
  private def generateInvalidToken: String = {
    // Just take a valid token and delete some characters from the signature
    generateValidToken().dropRight(5)
  }

  // ------------------------------------------------------------------------
  // 1. No cookie, no Authorization header => 403 Forbidden
  // ------------------------------------------------------------------------
  test("No cookie, no Authorization header => 403 with no authentication token") {
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Forbidden)
      assert(body.contains("No authentication token provided"))
    }
  }

  // ------------------------------------------------------------------------
  // 2. Cookie with valid token => 200 OK ("Authenticated")
  // ------------------------------------------------------------------------
  test("Cookie with valid token => 200 OK") {
    val validToken = generateValidToken()
    val cookie = RequestCookie("jwtToken", validToken)
    val req    = Request[IO](Method.GET, uri"/").addCookie(cookie)

    for {
      resp <- route.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assert(body.contains("Authenticated"))
    }
  }

  // ------------------------------------------------------------------------
  // 3. Cookie with invalid token => 403 Forbidden ("Invalid or tempered token")
  // ------------------------------------------------------------------------
  test("Cookie with invalid token => 403 Forbidden") {
    val invalidToken = generateInvalidToken
    val cookie = RequestCookie("jwtToken", invalidToken)
    val req    = Request[IO](Method.GET, uri"/").addCookie(cookie)

    for {
      resp <- route.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Forbidden)
      assert(body.contains("Invalid or tempered token"))
    }
  }

  // ------------------------------------------------------------------------
  // 4. Authorization header with valid "Bearer <token>" => 200 OK
  // ------------------------------------------------------------------------
  test("Authorization header with valid Bearer token => 200 OK") {
    val validToken = generateValidToken()
    val header = Headers(
      Header.Raw(org.http4s.headers.Authorization.name, s"Bearer $validToken")
    )
    val req = Request[IO](Method.GET, uri"/", headers = header)

    for {
      resp <- route.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assert(body.contains("Authenticated"))
    }
  }

  // ------------------------------------------------------------------------
  // 5. Authorization header with invalid token => 403 Forbidden
  // ------------------------------------------------------------------------
  test("Authorization header with invalid token => 403 Forbidden") {
    val invalidToken = generateInvalidToken
    val header = Headers(
      Header.Raw(org.http4s.headers.Authorization.name, s"Bearer $invalidToken")
    )
    val req = Request[IO](Method.GET, uri"/", headers = header)

    for {
      resp <- route.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Forbidden)
      assert(body.contains("Invalid or tempered token"))
    }
  }

  // ------------------------------------------------------------------------
  // 6. Authorization header not starting with "Bearer "
  // ------------------------------------------------------------------------
  test("""Authorization header missing "Bearer " => 403 Forbidden with "No valid Bearer token found"""") {
    val validToken = generateValidToken()
    // We'll prefix with "Token " instead of "Bearer "
    val header = Headers(
      Header.Raw(org.http4s.headers.Authorization.name, s"Token $validToken")
    )
    val req = Request[IO](Method.GET, uri"/", headers = header)

    for {
      resp <- route.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Forbidden)
      assert(body.contains("No valid Bearer token found"))
    }
  }
}

