package com.just.donate.api

import cats.effect.IO
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

class WithdrawalApiSpec extends CatsEffectSuite:
  private val Bank = "Bank"
  private val UnknownId = "unknown-id"
  private val UnknownAccount = "unknown-account"
  private val Description = "test-description"
  private val EarmarkEducation = "Education"
  private val EarmarkEducationDesc = "For education"

  private val repo = MemoryOrganisationRepository()
  private val routes = WithdrawalRoute.withdrawalRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound
  private val organisationRoute = OrganisationRoute.organisationApi(repo).orNotFound
  private val donationRoute = DonationRoute.donationRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound

  override def beforeEach(context: BeforeEach): Unit = repo.clear().unsafeRunSync()

  test("POST /withdraw/organisationId should return OK and update the organisation") {
    val actionsInit = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("0"))
    )
    val actionsDonate = Seq(
      ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, "Tom", "tom@gmail.com", Money("100"), None)
    )
    val actionsWithdraw = Seq(
      ApiAction.Withdraw(organisationId(NEW_ROOTS), paypalAccountName, Money("100"), Description, None)
    )
    val retrieve = ApiAction.ListAccounts(organisationId(NEW_ROOTS))
    for
      _ <- ApiRun.apiRun(organisationRoute, actionsInit)
      _ <- ApiRun.apiRun(donationRoute, actionsDonate)
      _ <- ApiRun.apiRun(routes, actionsWithdraw)
      res <- ApiRun.apiRun(organisationRoute, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      val accounts = res._2.get
      assertEquals(accounts.find(_.name == paypalAccountName).get.balance, Money.ZERO)
  }

  test("POST /withdraw/organisationId should return BadRequest if organisation not found") {
    val actions = Seq(
      ApiAction.Withdraw(UnknownId, paypalAccountName, Money("100"), Description, None)
    )
    val retrieve = ApiAction.Withdraw(UnknownId, paypalAccountName, Money("100"), Description, None)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /withdraw/organisationId should return BadRequest if account not found") {
    val actions = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.Withdraw(organisationId(NEW_ROOTS), UnknownAccount, Money("100"), Description, None)
    )
    val retrieve = ApiAction.Withdraw(organisationId(NEW_ROOTS), UnknownAccount, Money("100"), Description, None)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /withdraw/organisationId should return BadRequest if insufficient funds") {
    val actionsInit = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("50"))
    )
    val actionsWithdraw = Seq(
      ApiAction.Withdraw(organisationId(NEW_ROOTS), paypalAccountName, Money("100"), Description, None)
    )
    val retrieve = ApiAction.Withdraw(organisationId(NEW_ROOTS), paypalAccountName, Money("100"), Description, None)
    for
      _ <- ApiRun.apiRun(organisationRoute, actionsInit)
      res <- ApiRun.apiRun(routes, actionsWithdraw, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /withdraw/organisationId should return BadRequest if earmarking does not exist") {
    val actionsInit = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("100"))
    )
    val actionsWithdraw = Seq(
      ApiAction.Withdraw(organisationId(NEW_ROOTS), paypalAccountName, Money("100"), Description, Some("unknown"))
    )
    val retrieve = ApiAction.Withdraw(organisationId(NEW_ROOTS), paypalAccountName, Money("100"), Description, Some("unknown"))
    for
      _ <- ApiRun.apiRun(organisationRoute, actionsInit)
      res <- ApiRun.apiRun(routes, actionsWithdraw, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /withdraw/organisationId should handle withdrawal with earmarking") {
    val actionsInit = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("0")),
      ApiAction.AddEarmarking(organisationId(NEW_ROOTS), EarmarkEducation, EarmarkEducationDesc)
    )
    val actionsDonate = Seq(
      ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, "Tom", "tom@gmail.com", Money("100"), Some(EarmarkEducation))
    )
    val actionsWithdraw = Seq(
      ApiAction.Withdraw(organisationId(NEW_ROOTS), paypalAccountName, Money("100"), Description, Some(EarmarkEducation))
    )
    val retrieve = ApiAction.ListAccounts(organisationId(NEW_ROOTS))
    for
      _ <- ApiRun.apiRun(organisationRoute, actionsInit)
      _ <- ApiRun.apiRun(donationRoute, actionsDonate)
      _ <- ApiRun.apiRun(routes, actionsWithdraw)
      res <- ApiRun.apiRun(organisationRoute, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      val accounts = res._2.get
      assertEquals(accounts.find(_.name == paypalAccountName).get.balance, Money.ZERO)
  }

  test("POST /withdraw/organisationId should handle invalid request body") {
    val invalidRequest = Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS)))
      .withEntity("{invalid json}")
    for resp <- routes.run(invalidRequest)
    yield assertEquals(resp.status, Status.BadRequest)
  }
