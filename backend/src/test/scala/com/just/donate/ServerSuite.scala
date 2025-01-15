package com.just.donate

import cats.effect.{IO, Resource}
import com.just.donate.config.{AppConfig, Config}
import com.just.donate.db.mongo.*
import com.just.donate.notify.IEmailService
import com.just.donate.utils.ErrorLogger
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.Router
import org.http4s.implicits.*
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.mongodb.scala.MongoClient

class ServerSuite extends CatsEffectSuite {

  // --------------------------------------------------------
  // Mocks
  // --------------------------------------------------------
  private val mockConfig = mock(classOf[Config])
  private val mockOrgRepo = mock(classOf[MongoOrganisationRepository])
  private val mockPaypalRepo = mock(classOf[MongoPaypalRepository])
  private val mockUserRepo = mock(classOf[MongoUserRepository])
  private val mockErrorLogRepo = mock(classOf[MongoErrorLogRepository])
  private val mockEmailService = mock(classOf[IEmailService])
  private val mockErrorLogger = mock(classOf[ErrorLogger])

  override def beforeAll(): Unit = {
    // Example of stubbing the config
    when(mockConfig.mongoUri).thenReturn("mongodb://fakeHost:27017/test-db")
    super.beforeAll()
  }

  // --------------------------------------------------------
  // Example 1: Testing that mongoResource() can be used
  // --------------------------------------------------------
  test("mongoResource does not throw on acquire and release") {
    val resource: Resource[IO, MongoClient] = Server.mongoResource(mockConfig.mongoUri)
    // We just expect no exceptions
    resource.use { client =>
      // pretend to do something
      IO(client.getDatabase("my-test-database"))
    }.map { _ =>
      assert(true) // If it got here, success
    }
  }

  // --------------------------------------------------------
  // Example 2: Test a minimal route returns 200
  // --------------------------------------------------------
  test("Public route GET /public/test should return 200 OK") {
    // Create a minimal route for testing
    val testPublicRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "test" => Ok("Test route")
    }
    val routerUnderTest = Router(
      "public" -> testPublicRoute
    ).orNotFound

    // Make request to /public/test
    val req = Request[IO](method = Method.GET, uri = uri"/public/test")
    for {
      response <- routerUnderTest.run(req)
      body     <- response.as[String]
    } yield {
      assertEquals(response.status, Status.Ok)
      assert(body.contains("Test route"))
    }
  }

  // --------------------------------------------------------
  // Example 3: Unknown route returns 404
  // --------------------------------------------------------
  test("GET /public/nope returns 404 NotFound") {
    val testRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "existing" => Ok("Yes, I'm here")
    }
    val routerUnderTest = Router("public" -> testRoute).orNotFound

    // Make request to route that doesn't exist
    val req = Request[IO](method = Method.GET, uri = uri"/public/nope")
    for {
      response <- routerUnderTest.run(req)
    } yield {
      assertEquals(response.status, Status.NotFound)
    }
  }
}


