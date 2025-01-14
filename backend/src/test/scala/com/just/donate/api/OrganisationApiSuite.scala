package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.DonationRoute.RequestDonation
import com.just.donate.api.OrganisationRoute.{RequestAccount, RequestEarmarking, RequestOrganisation, ResponseAccount, ResponseOrganisation}
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.helper.TestHelper.*
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import com.just.donate.utils.Money
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
  private val donationRoute = DonationRoute.donationRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound

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
    val requestJson =
      Json.obj("name" -> Json.fromString("AccName"), "balance" -> Json.obj("amount" -> Json.fromString("100")))
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

  test("GET /organisation/{id}/account/list should return Ok and all accounts split by earmarking") {
    val orgRequest = RequestOrganisation("Org1")

    val payPalAccount = RequestAccount("PayPal", Money("0.0"))
    val bankAccount = RequestAccount("Bank", Money("0.0"))

    val educationEarmarking = RequestEarmarking("Education", "For education")
    val healthEarmarking = RequestEarmarking("Health", "For health")

    val donateRequest1 = RequestDonation("Donor1", "donor1@example.org", Money("10.0"), Some(educationEarmarking.name))
    val donateRequest2 = RequestDonation("Donor1", "donor2@example.org", Money("11.0"), Some(healthEarmarking.name))
    val donateRequest3 = RequestDonation("Donor2", "donor2@example.org", Money("12.0"), Some(educationEarmarking.name))
    val donateRequest4 = RequestDonation("Donor2", "donor3@example.org", Money("13.0"), Some(educationEarmarking.name))
    val donateRequest5 = RequestDonation("Donor3", "donor3@example.org", Money("14.0"), Some(healthEarmarking.name))

    val reqCreate = Request[IO](Method.POST, uri"/").withEntity(orgRequest)

    val reqPayPalAccount =
      Request[IO](Method.POST, testUri(organisationId("Org1"), "account")).withEntity(payPalAccount)
    val reqBankAccount = Request[IO](Method.POST, testUri(organisationId("Org1"), "account")).withEntity(bankAccount)

    val reqEducationEarmarking =
      Request[IO](Method.POST, testUri(organisationId("Org1"), "earmarking")).withEntity(educationEarmarking)
    val reqHealthEarmarking =
      Request[IO](Method.POST, testUri(organisationId("Org1"), "earmarking")).withEntity(healthEarmarking)

    val reqDonate1 = Request[IO](Method.POST, testUri(organisationId("Org1"), "account", "PayPal")).withEntity(donateRequest1)
    val reqDonate2 = Request[IO](Method.POST, testUri(organisationId("Org1"), "account", "PayPal")).withEntity(donateRequest2)
    val reqDonate3 = Request[IO](Method.POST, testUri(organisationId("Org1"), "account", "Bank")).withEntity(donateRequest3)
    val reqDonate4 = Request[IO](Method.POST, testUri(organisationId("Org1"), "account", "PayPal")).withEntity(donateRequest4)
    val reqDonate5 = Request[IO](Method.POST, testUri(organisationId("Org1"), "account", "Bank")).withEntity(donateRequest5)

    val reqList = Request[IO](Method.GET, testUri(organisationId("Org1"), "account", "list"))

    for
      _ <- routes.run(reqCreate)
      _ <- routes.run(reqPayPalAccount)
      _ <- routes.run(reqBankAccount)
      _ <- routes.run(reqEducationEarmarking)
      _ <- routes.run(reqHealthEarmarking)
      resp1 <- donationRoute.run(reqDonate1)
      _ <- donationRoute.run(reqDonate2)
      _ <- donationRoute.run(reqDonate3)
      _ <- donationRoute.run(reqDonate4)
      _ <- donationRoute.run(reqDonate5)
      respList <- routes.run(reqList)
      _ <- IO.println(respList)
      body <- respList.as[List[ResponseAccount]]
    yield
      assertEquals(respList.status, Status.Ok)
      assertEquals(resp1.status, Status.Ok)

      assertEquals(body.size, 2)
      assertEquals(body.head.name, "PayPal")
      assertEquals(body.head.balance, Money("34.0"))
      assertEquals(body.head.byEarmarking.size, 2)
      assertEquals(body.head.byEarmarking.head._1, "Education")
      assertEquals(body.head.byEarmarking.head._2, Money("23.0"))
      assertEquals(body.head.byEarmarking(1)._1, "Health")
      assertEquals(body.head.byEarmarking(1)._2, Money("11.0"))

      assertEquals(body(1).name, "Bank")
      assertEquals(body(1).balance, Money("26.0"))
      assertEquals(body(1).byEarmarking.size, 2)
      assertEquals(body(1).byEarmarking.head._1, "Education")
      assertEquals(body(1).byEarmarking.head._2, Money("12.0"))
      assertEquals(body(1).byEarmarking(1)._1, "Health")
      assertEquals(body(1).byEarmarking(1)._2, Money("14.0"))
  }
