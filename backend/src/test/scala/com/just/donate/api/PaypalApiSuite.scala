package com.just.donate.api

import cats.effect.IO
import com.just.donate.*
import com.just.donate.mocks.paypal.MongoPaypalRepositoryMock
import munit.CatsEffectSuite
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Status, UrlForm}

import java.lang.Thread.sleep

class PaypalApiSuite extends CatsEffectSuite:

  sleep(1); // Sleep for 1 second to avoid port conflict with other tests

  private val fakeRepo = new MongoPaypalRepositoryMock(null)

  private val routes = PaypalRoute.paypalRoute(fakeRepo).orNotFound

  test("GET / should list all IPNs in the DB") {
    val req = Request[IO](Method.GET, uri"/")
    print(req)

    for
      resp <- routes.run(req)
      body <- resp.as[String] // extract the body as a string
    yield
      assertEquals(resp.status, Status.Ok)
      assert(body.contains("test"))
  }

  test("POST / should insert a new IPN into the repo") {
    // TODO: Test further to check whether a new donation is generated in the correct account in Organization
    // Make a form-data POST
    val formData = UrlForm("txn_id" -> "ABC123", "custom" -> "Something")

    val req = Request[IO](Method.POST, uri"/").withEntity(formData)

    for
      resp <- routes.run(req)
      body <- resp.as[String]
      _ <- IO(assertEquals(resp.status, Status.Ok))
      _ <- IO(assert(body.contains("IPN Payload received")))
      allDb <- fakeRepo.findAll()
    yield
      print(body)
      assertEquals(allDb.size, 2)
      assert(allDb.tail.head.payload.contains("txn_id -> Chain(ABC123)"))
      assert(allDb.tail.head.payload.contains("custom -> Chain(Something)"))
  }
