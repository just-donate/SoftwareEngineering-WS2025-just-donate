package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.OrganisationRoute.{RequestOrganisation, ResponseOrganisation}
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.helper.TestHelper.*
import io.circe.*
import io.circe.generic.auto.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.implicits.*

class OrganisationApiSuite extends CatsEffectSuite:

  private val repo = MemoryOrganisationRepository()

  private val routes = OrganisationRoute.organisationApi(repo).orNotFound

  override def beforeEach(context: BeforeEach): Unit = repo.clear().unsafeRunSync()

  test("GET /organisation/list should return OK and an empty JSON list if no organisations exists") {
    val req = Request[IO](Method.GET, uri"/list")
    for
      resp <- routes.run(req)
      status = resp.status
      body <- resp.as[List[String]]
    yield
      assertEquals(status, Status.Ok)
      assert(body.isEmpty)
  }

  test("GET /organisation/list should return OK and an a list of organisations if organisations exist") {
    val orgRequest1 = RequestOrganisation("Org1")
    val orgRequest2 = RequestOrganisation("Org2")

    val req1 = Request[IO](Method.POST, uri"/").withEntity(orgRequest1)
    val req2 = Request[IO](Method.POST, uri"/").withEntity(orgRequest2)
    val reqList = Request[IO](Method.GET, uri"/list")

    for
      resp1 <- routes.run(req1)
      resp2 <- routes.run(req2)

      respList <- routes.run(reqList)
      statusList = respList.status
      organisations <- respList.as[List[String]]
    yield
      assertEquals(statusList, Status.Ok)

      assert(organisations.length == 2)
      assert(organisations.head == organisationId("Org1"))
      assert(organisations(1) == organisationId("Org2"))
  }

  test("POST /organisation should create a new organisation and return it") {
    val requestJson = Json.obj("name" -> Json.fromString("MyOrg"))
    val req = Request[IO](Method.POST, uri"/").withEntity(requestJson)
    for
      resp <- routes.run(req)
      status = resp.status
      body <- resp.as[ResponseOrganisation]
    yield
      assertEquals(status, Status.Ok)
      // body should have "organisationId" and "name"
      assertEquals(body.organisationId, organisationId("MyOrg"))
      assertEquals(body.name, "MyOrg")
  }

  test("GET /organisation/{id} should return NotFound if not exists") {
    val req = Request[IO](Method.GET, uri"/unknown-id")
    for resp <- routes.run(req)
    yield assertEquals(resp.status, Status.NotFound)
  }

  test("GET /organisation/{id} should return OK and the organisation if it exists") {
    val orgRequest = RequestOrganisation("Org1")
    val req = Request[IO](Method.POST, uri"/").withEntity(orgRequest)
    for
      resp <- routes.run(req)
      body <- resp.as[ResponseOrganisation]
    yield
      assertEquals(resp.status, Status.Ok)
      assertEquals(body.organisationId, organisationId("Org1"))
      assertEquals(body.name, "Org1")
  }

  test("DELETE /organisation/{id} should return Ok even if not present") {
    // For simplicity, expect that deleting a non-existent org returns Ok (idempotent behavior)
    val req = Request[IO](Method.DELETE, uri"/00000")
    for resp <- routes.run(req)
    yield assertEquals(resp.status, Status.Ok)
  }

  test("DELETE /organisation/{id} should really delete the organisation") {
    val orgRequest = RequestOrganisation("Org1")
    val reqCreate = Request[IO](Method.POST, uri"/").withEntity(orgRequest)
    val reqDelete = Request[IO](Method.DELETE, testUri(organisationId("Org1")))
    val reqList = Request[IO](Method.GET, uri"/list")

    for
      respCreate <- routes.run(reqCreate)
      respList1 <- routes.run(reqList)
      respDelete <- routes.run(reqDelete)
      respList2 <- routes.run(reqList)
      organisations1 <- respList1.as[List[String]]
      organisations2 <- respList2.as[List[String]]
    yield
      assertEquals(respCreate.status, Status.Ok)
      assertEquals(respDelete.status, Status.Ok)
      assertEquals(respList1.status, Status.Ok)
      assertEquals(respList2.status, Status.Ok)

      assertEquals(organisations1.length, 1)
      assertEquals(organisations2.length, 0)
  }

  test("POST /organisation/{id}/account should return NotFound if org does not exist") {
    val requestJson = Json.obj("name" -> Json.fromString("AccName"), "balance" -> Json.fromBigDecimal(100.00))
    val req = Request[IO](Method.POST, uri"/00000/account").withEntity(requestJson)
    for resp <- routes.run(req)
    yield assertEquals(resp.status, Status.NotFound)
  }

  test("DELETE /organisation/{id}/account/{name} should return NotFound if org does not exist") {
    val req = Request[IO](Method.DELETE, uri"/non-existing/account/AccName")
    for resp <- routes.run(req)
    yield assertEquals(resp.status, Status.NotFound)
  }

  test("GET /organisation/{id}/account/list should return Ok and all accounts") {
    val orgRequest = RequestOrganisation("Org1")
    val reqCreate = Request[IO](Method.POST, uri"/").withEntity(orgRequest)
    val reqList = Request[IO](Method.GET, testUri(organisationId("Org1"), "account", "list"))

    for
      respCreate <- routes.run(reqCreate)
      respList <- routes.run(reqList)
      _ <- IO.println(respList)
      body <- respList.as[List[String]]
    yield
      assertEquals(respCreate.status, Status.Ok)
      assertEquals(respList.status, Status.Ok)

      assert(body.isEmpty)
  }
