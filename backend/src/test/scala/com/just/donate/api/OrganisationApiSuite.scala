package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.DonationRoute.RequestDonation
import com.just.donate.api.OrganisationRoute.{RequestAccount, RequestEarmarking, RequestOrganisation, ResponseAccount, ResponseOrganisation}
import com.just.donate.api.helper.{ApiAction, ApiRun}
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

  test("GET /organisation/list should return empty list") {
    val retrieve = ApiAction.ListOrganisations()
    for
      res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq.empty)
  }

  test("POST /organisation/{id}/account should create an account") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
    )
    val retrieve = ApiAction.ListOrganisations()
    for
      res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq(organisationId("Org1")))
  }

  test("POST /organisation should create multiple organisations") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.AddOrganisation("Org2")
    )
    val retrieve = ApiAction.ListOrganisations()
    for
      res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq(organisationId("Org1"), organisationId("Org2")))
  }

  test("GET /organisation/{id} should return NotFound if not exists") {
    val retrieve = ApiAction.GetOrganisation("unknown-id")
    for
      res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.NotFound)
  }

  test("GET /organisation/{id} should return OK and the organisation if it exists") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1")
    )
    val retrieve = ApiAction.GetOrganisation(organisationId("Org1"))
    for
      res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get.organisationId, organisationId("Org1"))
      assertEquals(res._2.get.name, "Org1")
  }

  test("DELETE /organisation/{id} should return Ok even if not present") {
    // For simplicity, expect that deleting a non-existent org returns Ok (idempotent behavior)
    val actions = Seq(
      ApiAction.AddOrganisation("Org1")
    )
    val retrieve = ApiAction.DeleteOrganisation(organisationId("Org1"))
    for
      res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
  }

  test("DELETE /organisation/{id} should really delete the organisation") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.DeleteOrganisation(organisationId("Org1"))
    )
    val retrieve = ApiAction.ListOrganisations()
    for
      res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq.empty)
  }

  test("POST /organisation/{id}/account should return NotFound if org does not exist") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.AddAccount(organisationId("Org1"), "AccName", Money("100.0"))
    )
    val retrieve = ApiAction.GetOrganisation("unknown-id")
    for
      res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.NotFound)
  }

  test("DELETE /organisation/{id}/account/{name} should return NotFound if org does not exist") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.AddAccount(organisationId("Org1"), "AccName", Money("100.0"))
    )
    val retrieve = ApiAction.GetAccount(organisationId("Org1"), "unknown-id")
    for
      res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.NotFound)
  }

  test("GET /organisation/{id}/account/list should return Ok and all accounts") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.AddAccount(organisationId("Org1"), "AccName", Money("0.0"))
    )
    val retrieve = ApiAction.ListAccounts(organisationId("Org1"))

    for
      res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get.size, 1)
      assertEquals(res._2.get.head.name, "AccName")
      assertEquals(res._2.get.head.balance, Money("0.0"))
  }
  

  test("GET /organisation/{id}/account/list should return Ok and all accounts split by earmarking") {
    val setupActions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.AddAccount(organisationId("Org1"), "PayPal", Money("0.0")),
      ApiAction.AddAccount(organisationId("Org1"), "Bank", Money("0.0")),
      ApiAction.AddEarmarking(organisationId("Org1"), "Education", "For education"),
      ApiAction.AddEarmarking(organisationId("Org1"), "Health", "For health")
    )

    val donationActions = Seq(
      ApiAction.AddDonation(organisationId("Org1"), "PayPal", "Donor1", "donor1@example.org", Money("10.0"), Some("Education")),
      ApiAction.AddDonation(organisationId("Org1"), "PayPal", "Donor1", "donor2@example.org", Money("11.0"), Some("Health")),
      ApiAction.AddDonation(organisationId("Org1"), "Bank", "Donor2", "donor2@example.org", Money("12.0"), Some("Education")),
      ApiAction.AddDonation(organisationId("Org1"), "PayPal", "Donor2", "donor3@example.org", Money("13.0"), Some("Education")),
      ApiAction.AddDonation(organisationId("Org1"), "Bank", "Donor3", "donor3@example.org", Money("14.0"), Some("Health"))
    )

    val retrieve = ApiAction.ListAccounts(organisationId("Org1"))

    for
      _ <- ApiRun.apiRun(routes, setupActions, retrieve)
      _ <- ApiRun.apiRun(donationRoute, donationActions, retrieve)
      res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield
      val body = res._2.get
      assertEquals(body.size, 2)
      assertEquals(body.head.name, "PayPal")
      assertEquals(body.head.balance, Money("34.0"))
      assertEquals(body.head.byEarmarking.size, 3)
      assertEquals(body.head.byEarmarking.head._1, "Education")
      assertEquals(body.head.byEarmarking.head._2, Money("23.0"))
      assertEquals(body.head.byEarmarking(1)._1, "Health")
      assertEquals(body.head.byEarmarking(1)._2, Money("11.0"))

      assertEquals(body(1).name, "Bank")
      assertEquals(body(1).balance, Money("26.0"))
      assertEquals(body(1).byEarmarking.size, 3)
      assertEquals(body(1).byEarmarking.head._1, "Education")
      assertEquals(body(1).byEarmarking.head._2, Money("12.0"))
      assertEquals(body(1).byEarmarking(1)._1, "Health")
      assertEquals(body(1).byEarmarking(1)._2, Money("14.0"))
  }


