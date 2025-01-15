package com.just.donate.api

import cats.effect.IO
import com.just.donate.config.{AppEnvironment, Config}
import com.just.donate.db.Repository
import com.just.donate.models.user.{Roles, User}
import com.just.donate.utils.CryptoUtils
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*
import org.mockito.{Mockito, MockitoSugar}
import pdi.jwt.{Jwt, JwtAlgorithm}

class LoginRouteSuite extends CatsEffectSuite with MockitoSugar {

  // ----------------------------------------------------------------------
  // Helpers
  // ----------------------------------------------------------------------
  private val mockConfig = Mockito.mock(classOf[Config])
  private val mockUserRepo = Mockito.mock(classOf[Repository[String, User]])

  // Our route under test
  private val loginRoute: HttpRoutes[IO] = LoginRoute.loginRoute(mockConfig, mockUserRepo)

  // Convert HttpRoutes to HttpApp for test requests
  private val httpApp: HttpApp[IO] = loginRoute.orNotFound

  // We’ll configure some helper functions and constants
  private val plainPassword = "secret123"
  private val hashedPassword = CryptoUtils.hashPassword(plainPassword) // Real hash

  // A sample active user with a known password ( hashedPassword ) 
  private val sampleUser = User(
    email = "user@example.com",
    password = hashedPassword,
    role = Roles.USER.toString,
    active = true,
    orgId = "some-org"
  )

  override def beforeEach(context: BeforeEach): Unit = {
    reset(mockConfig, mockUserRepo)
    super.beforeEach(context)
  }

  // ----------------------------------------------------------------------
  // 1) Login success => returns 200 OK, sets Cookie
  // ----------------------------------------------------------------------
  test("POST / login => success with correct credentials & active user => 200 + jwt cookie") {
    // userRepo returns Some(user)
    Mockito.doReturn(IO.pure(Some(sampleUser)))
      .when(mockUserRepo)
      .findById("user@example.com")

    // environment = DEVELOPMENT => not secure, SameSite=Strict
    Mockito.doReturn(AppEnvironment.DEVELOPMENT)
      .when(mockConfig)
      .environment

    val jsonBody =
      s"""
         |{
         | "username": "user@example.com",
         | "password": "$plainPassword",
         | "orgId": "some-org"
         |}
         |""".stripMargin

    val req = Request[IO](method = Method.POST, uri = uri"/")
      .withEntity(jsonBody)

    for {
      resp <- httpApp.run(req)
      body <- resp.as[String]
    } yield {
      // Expect 200 OK
      assertEquals(resp.status, Status.Ok)
      // Confirm there's a cookie with name "jwtToken"
      val maybeCookie = resp.cookies.find(_.name == "jwtToken")
      assert(maybeCookie.isDefined, "Response must have a 'jwtToken' cookie")
      val cookie = maybeCookie.get

      // Because environment is DEVELOPMENT:
      //   cookie.secure should be false
      //   cookie.sameSite should be Some(SameSite.Strict)
      assert(!cookie.secure)
      assertEquals(cookie.sameSite, Some(SameSite.Strict))

      // Body is the raw JWT token
      // We can do a quick check that it's a valid (decodable) token
      val secretKey = sys.env.getOrElse("JWT_SECRET_KEY", "HAu/gwjy5124uMaX9wTAEPPXYDwsCYIWeZ7JnpRTRRk=")
      val algo = JwtAlgorithm.HS256
      val decodeResult = Jwt.decode(cookie.content, secretKey, Seq(algo))
      assert(decodeResult.isSuccess, s"JWT decode must succeed, got $decodeResult")

      // Check the JSON body also includes the token
      assert(body.trim.nonEmpty, "Response body must contain the token string")
    }
  }

  // ----------------------------------------------------------------------
  // 2) Invalid credentials => 403
  // ----------------------------------------------------------------------
  test("POST / login => 403 if password is incorrect") {
    // Return the user, but the actual hashed password won't match the given "wrongPass"
    Mockito.doReturn(IO.pure(Some(sampleUser)))
      .when(mockUserRepo)
      .findById("user@example.com")

    Mockito.doReturn(AppEnvironment.DEVELOPMENT)
      .when(mockConfig)
      .environment

    val jsonBody =
      s"""
         |{
         | "username": "user@example.com",
         | "password": "wrongPass",
         | "orgId": "some-org"
         |}
         |""".stripMargin

    val req = Request[IO](Method.POST, uri = uri"/")
      .withEntity(jsonBody)

    for {
      resp <- httpApp.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Forbidden)
      assert(body.contains("Invalid credentials"))
    }
  }

  // ----------------------------------------------------------------------
  // 3) User not found => 403
  // ----------------------------------------------------------------------
  test("POST / login => 403 if user is not found") {
    Mockito.doReturn(IO.pure(None))
      .when(mockUserRepo)
      .findById("ghost@example.com")

    Mockito.doReturn(AppEnvironment.DEVELOPMENT)
      .when(mockConfig)
      .environment

    val jsonBody =
      s"""
         |{
         | "username": "ghost@example.com",
         | "password": "$plainPassword",
         | "orgId": "whatever"
         |}
         |""".stripMargin

    val req = Request[IO](Method.POST, uri = uri"/").withEntity(jsonBody)

    for {
      resp <- httpApp.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Forbidden)
      assert(body.contains("Invalid credentials"))
    }
  }

  // ----------------------------------------------------------------------
  // 4) User is inactive => 403
  // ----------------------------------------------------------------------
  test("POST / login => 403 if user is inactive") {
    val inactiveUser = sampleUser.copy(active = false)
    Mockito.doReturn(IO.pure(Some(inactiveUser)))
      .when(mockUserRepo)
      .findById("inactive@example.com")

    Mockito.doReturn(AppEnvironment.DEVELOPMENT)
      .when(mockConfig)
      .environment

    val jsonBody =
      s"""
         |{
         | "username": "inactive@example.com",
         | "password": "$plainPassword",
         | "orgId": "some-org"
         |}
         |""".stripMargin

    val req = Request[IO](Method.POST, uri = uri"/").withEntity(jsonBody)

    for {
      resp <- httpApp.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Forbidden)
      assert(body.contains("Invalid credentials"))
    }
  }

  // ----------------------------------------------------------------------
  // 5) Malformed JSON => 401 Unauthorized
  // ----------------------------------------------------------------------
  test("POST / login => 401 if malformed request body") {
    // e.g. missing quotes, invalid JSON, etc.
    val invalidJsonBody =
      """
        |{
        |  username: user@example.com,  // missing quotes => invalid JSON
        |  password: secret
        |}
        |""".stripMargin

    Mockito.doReturn(AppEnvironment.DEVELOPMENT)
      .when(mockConfig)
      .environment

    val req = Request[IO](Method.POST, uri = uri"/")
      .withEntity(invalidJsonBody)

    for {
      resp <- httpApp.run(req)
    } yield {
      assertEquals(resp.status, Status.Unauthorized)
      // Check the WWW-Authenticate header
      val wwwAuth = resp.headers.get[headers.`WWW-Authenticate`]
      assert(wwwAuth.isDefined, "Should have a WWW-Authenticate header")
      val challenge = wwwAuth.get.value
      assert(challenge.contains("Basic"), "Should mention Basic")
      assert(challenge.contains("Malformed request body"), "Should mention Malformed request body")
    }
  }

  // ----------------------------------------------------------------------
  // 6) Production environment => cookie is secure, sameSite=None
  // ----------------------------------------------------------------------
  test("POST / login => cookie is secure if environment == PRODUCTION") {
    val prodUser = sampleUser.copy(email = "prod@example.com")

    Mockito.doReturn(IO.pure(Some(prodUser)))
      .when(mockUserRepo)
      .findById("prod@example.com")

    // environment = PRODUCTION => secure=true, sameSite=None
    Mockito.doReturn(AppEnvironment.PRODUCTION)
      .when(mockConfig)
      .environment

    val jsonBody =
      s"""
         |{
         | "username": "prod@example.com",
         | "password": "$plainPassword",
         | "orgId": "some-org"
         |}
         |""".stripMargin

    val req = Request[IO](Method.POST, uri = uri"/").withEntity(jsonBody)

    for {
      resp <- httpApp.run(req)
    } yield {
      val cookie = resp.cookies.find(_.name == "jwtToken")
      assert(cookie.isDefined, "Should set 'jwtToken' cookie in production env")
      assert(cookie.get.secure, "Cookie must be 'secure' in production")
      assertEquals(cookie.get.sameSite, Some(SameSite.None))
    }
  }

}

