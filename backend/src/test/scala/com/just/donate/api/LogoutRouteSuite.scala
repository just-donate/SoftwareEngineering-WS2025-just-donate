package com.just.donate.api

import cats.effect.IO
import com.just.donate.config.{AppEnvironment, Config}
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*
import org.mockito.{Mockito, MockitoSugar}

class LogoutRouteSuite extends CatsEffectSuite with MockitoSugar {

  private val mockConfig = Mockito.mock(classOf[Config])

  // We'll create an HttpApp from the LogoutRoute for testing
  private def createLogoutApp(env: AppEnvironment): HttpApp[IO] = {
    reset(mockConfig)
    
    Mockito.doReturn(env)
      .when(mockConfig)
      .environment

    LogoutRoute.logoutRoute(mockConfig).orNotFound
  }

  // --------------------------------------------------------------------------
  // 1) POST / => In DEVELOPMENT => cookie.secure=false, sameSite=Strict
  // --------------------------------------------------------------------------
  test("POST / => Logout sets expired cookie in DEVELOPMENT") {
    val httpApp = createLogoutApp(AppEnvironment.DEVELOPMENT)
    val req = Request[IO](Method.POST, uri"/")

    for {
      resp <- httpApp.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assert(body.contains("Logout successful"))

      // Check the cookie properties
      val maybeCookie = resp.cookies.find(_.name == "jwtToken")
      assert(maybeCookie.isDefined, "Should have 'jwtToken' cookie")

      val cookie = maybeCookie.get
      assertEquals(cookie.content, "")
      // In DEVELOPMENT => secure=false, sameSite=Strict
      assertEquals(cookie.secure, false)
      assertEquals(cookie.sameSite, Some(SameSite.Strict))
      assertEquals(cookie.maxAge, Some(0L))  // expires immediately
    }
  }

  // --------------------------------------------------------------------------
  // 2) POST / => In PRODUCTION => cookie.secure=true, sameSite=None
  // --------------------------------------------------------------------------
  test("POST / => Logout sets expired cookie in PRODUCTION") {
    val httpApp = createLogoutApp(AppEnvironment.PRODUCTION)
    val req = Request[IO](Method.POST, uri"/")

    for {
      resp <- httpApp.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assert(body.contains("Logout successful"))

      // Check the cookie properties
      val maybeCookie = resp.cookies.find(_.name == "jwtToken")
      assert(maybeCookie.isDefined, "Should have 'jwtToken' cookie")

      val cookie = maybeCookie.get
      assertEquals(cookie.content, "")
      // In PRODUCTION => secure=true, sameSite=None
      assertEquals(cookie.secure, true)
      assertEquals(cookie.sameSite, Some(SameSite.None))
      assertEquals(cookie.maxAge, Some(0L)) // expires immediately
    }
  }
}

