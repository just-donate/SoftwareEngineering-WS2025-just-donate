package com.just.donate.api

import cats.effect.{IO, Resource}
import com.just.donate.*
import com.just.donate.mocks.client.MockHttpClient
import com.just.donate.mocks.paypal.PaypalRepositoryMock
import munit.CatsEffectSuite
import org.http4s.client.Client
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Response, Status, UrlForm}
import scala.concurrent.duration._

import java.lang.Thread.sleep

class PaypalApiSuite extends CatsEffectSuite:

  sleep(1); // Sleep for 1 second to avoid port conflict with other tests

  private val fakeRepo = new PaypalRepositoryMock(null)

  override def beforeEach(context: BeforeEach): Unit = {
    fakeRepo.reset()
  }

  test("POST / should handle PayPal IPN verification is Verified") {
    val mockClientResource: Resource[IO, Client[IO]] = MockHttpClient.resource {
      case req if req.uri.toString.contains("paypal.com") =>
        Resource.pure(Response[IO](Status.Ok).withEntity("VERIFIED"))
      case req if req.uri.toString.contains("error.com") =>
        Resource.pure(Response[IO](Status.InternalServerError))
    }
    val formData = UrlForm("txn_id" -> "ABC123", "custom" -> "Something")
    val rawBody = "txn_id=ABC123&custom=Something"

    mockClientResource.use { mockClient =>
      val routes = PaypalRoute.paypalRoute(fakeRepo, mockClient).orNotFound

      val req = Request[IO](Method.POST, uri"/").withEntity(formData)

      for {
        resp <- routes.run(req)
        body <- resp.as[String]
        _ <- IO.sleep(100.milli) // Sleep to allow async processing to complete
        _ <- IO {
          assertEquals(resp.status, Status.Ok)
        }
        allDb <- fakeRepo.findAll()
      } yield {
        assertEquals(body, "")
        assertEquals(allDb.length, 2)
        assertEquals(allDb.tail.head.payload, rawBody)
      }
    }
  }

  test("POST / should handle PayPal IPN verification when the response is not Verified") {
    val mockClientResource: Resource[IO, Client[IO]] = MockHttpClient.resource {
      case req if req.uri.toString.contains("paypal.com") =>
        Resource.pure(Response[IO](Status.Ok).withEntity("INVALID"))
      case req if req.uri.toString.contains("error.com") =>
        Resource.pure(Response[IO](Status.InternalServerError))
    }

    val formData = UrlForm("txn_id" -> "ABC123", "custom" -> "Something")
    val rawBody = "txn_id=ABC123&custom=Something"

    mockClientResource.use { mockClient =>
      val routes = PaypalRoute.paypalRoute(fakeRepo, mockClient).orNotFound

      val req = Request[IO](Method.POST, uri"/").withEntity(formData)

      for {
        resp <- routes.run(req)
        body <- resp.as[String]
        _ <- IO.sleep(100.milli) // Sleep to allow async processing to complete
        _ <- IO {
          assertEquals(resp.status, Status.Ok)
        }
        allDb <- fakeRepo.findAll()
      } yield {
        assertEquals(body, "")
        assertEquals(allDb.length, 1)
        assertEquals(allDb.head.payload, "test")
      }
    }
  }
