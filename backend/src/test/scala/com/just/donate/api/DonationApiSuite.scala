package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.DonationRoute.RequestDonation
import com.just.donate.api.PaypalRoute.paypalAccountName
import com.just.donate.api.helper.{ApiAction, ApiRun}
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.helper.TestHelper.*
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import com.just.donate.utils.Money
import munit.CatsEffectSuite
import org.http4s.*

class DonationApiSuite extends CatsEffectSuite:
  private val Bank = "Bank"
  private val UnknownId = "unknown-id"
  private val UnknownAccount = "unknown-account"
  private val DonorName = "MyDonor"
  private val DonorEmail = "mydonor@example.org"
  private val EarmarkEducation = "Education"
  private val EarmarkEducationDesc = "For education"

  private val repo = MemoryOrganisationRepository()
  private val routes = DonationRoute.donationRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound
  private val organisationRoute = OrganisationRoute.organisationApi(repo).orNotFound

  override def beforeEach(context: BeforeEach): Unit = repo.clear().unsafeRunSync()

  test("POST /donate/organisationId/account/accountName should return OK and update the organisation") {
    val actionsInit = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("0"))
    )
    val actionsDonate = Seq(
      ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, DonorName, DonorEmail, Money("100"), None)
    )
    val retrieve = ApiAction.ListAccounts(organisationId(NEW_ROOTS))
    for
      _ <- ApiRun.apiRun(organisationRoute, actionsInit)
      _ <- ApiRun.apiRun(routes, actionsDonate)
      res <- ApiRun.apiRun(organisationRoute, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      val accounts = res._2.get
      assertEquals(accounts.find(_.name == paypalAccountName).get.balance, Money("100"))
  }

  test("POST /donate/organisationId/account/accountName should return BadRequest if organisation not found") {
    val actions = Seq(
      ApiAction.AddDonation(UnknownId, paypalAccountName, DonorName, DonorEmail, Money("100"), None)
    )
    val retrieve = ApiAction.AddDonation(UnknownId, paypalAccountName, DonorName, DonorEmail, Money("100"), None)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /donate/organisationId/account/accountName should return NotFound if account not found") {
    val actions = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddDonation(organisationId(NEW_ROOTS), UnknownAccount, DonorName, DonorEmail, Money("100"), None)
    )
    val retrieve = ApiAction.AddDonation(organisationId(NEW_ROOTS), UnknownAccount, DonorName, DonorEmail, Money("100"), None)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /donate/organisationId/account/accountName should return BadRequest if earmarking does not exist") {
    val actions = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("0")),
      ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, DonorName, DonorEmail, Money("100"), Some("unknown"))
    )
    val retrieve = ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, DonorName, DonorEmail, Money("100"), Some("unknown"))
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /donate/organisationId/account/accountName should handle invalid request body") {
    val invalidRequest = Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS), "account", paypalAccountName))
      .withEntity("{invalid json}")
    for resp <- routes.run(invalidRequest)
    yield assertEquals(resp.status, Status.BadRequest)
  }

  test("GET /donations should return empty list for new organisation") {
    val actions = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS)
    )
    val retrieve = ApiAction.ListDonations(organisationId(NEW_ROOTS))
    for
      _ <- ApiRun.apiRun(organisationRoute, actions)
      res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get.donations.size, 0)
  }

  test("GET /donations should return NotFound for unknown organisation") {
    val retrieve = ApiAction.ListDonations(UnknownId)
    for res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield assertEquals(res._1, Status.NotFound)
  }

  test("GET /donations should return list of donations with earmarking") {
    val actionsInit = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("0")),
      ApiAction.AddEarmarking(organisationId(NEW_ROOTS), EarmarkEducation, EarmarkEducationDesc)
    )
    val actionsDonate = Seq(
      ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, DonorName, DonorEmail, Money("100"), Some(EarmarkEducation))
    )
    val retrieve = ApiAction.ListDonations(organisationId(NEW_ROOTS))
    for
      _ <- ApiRun.apiRun(organisationRoute, actionsInit)
      _ <- ApiRun.apiRun(routes, actionsDonate)
      res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      val donations = res._2.get.donations
      assertEquals(donations.size, 1)
      assertEquals(donations.head.amount, Money("100"))
      assertEquals(donations.head.donor.name, DonorName)
      assertEquals(donations.head.donor.email, DonorEmail)
      assertEquals(donations.head.earmarking, Some(EarmarkEducation))
  }

  test("POST /donate should reuse existing donor") {
    val actionsInit = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("0"))
    )
    val actionsDonate = Seq(
      ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, DonorName, DonorEmail, Money("100"), None),
      ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, s"$DonorName 2", DonorEmail, Money("50"), None)
    )
    val retrieve = ApiAction.ListDonations(organisationId(NEW_ROOTS))
    for
      _ <- ApiRun.apiRun(organisationRoute, actionsInit)
      _ <- ApiRun.apiRun(routes, actionsDonate)
      res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      val donations = res._2.get.donations
      assertEquals(donations.size, 2)
      assertEquals(donations.head.donor.id, donations(1).donor.id)
      assertEquals(donations.head.donor.email, donations(1).donor.email)
  }
