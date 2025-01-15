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

class TransferApiSuite extends CatsEffectSuite:
  private val Bank = "Bank"
  private val UnknownId = "unknown-id"
  private val UnknownAccount = "unknown-account"

  private val repo = MemoryOrganisationRepository()
  private val routes = TransferRoute.transferRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound
  private val donationRoute = DonationRoute.donationRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound
  private val organisationRoute = OrganisationRoute.organisationApi(repo).orNotFound

  override def beforeEach(context: BeforeEach): Unit = repo.clear().unsafeRunSync()

  test("POST /transfer/organisationId should return NotFound if organisation does not exist") {
    val actions = Seq(
      ApiAction.Transfer(UnknownId, paypalAccountName, Bank, Money("100"))
    )
    val retrieve = ApiAction.Transfer(UnknownId, paypalAccountName, Bank, Money("100"))
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /transfer/organisationId should return BadRequest if source account does not exist") {
    val actions = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.Transfer(organisationId(NEW_ROOTS), UnknownAccount, Bank, Money("100"))
    )
    val retrieve = ApiAction.Transfer(organisationId(NEW_ROOTS), UnknownAccount, Bank, Money("100"))
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /transfer/organisationId should return BadRequest if target account does not exist") {
    val actions = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("100")),
      ApiAction.Transfer(organisationId(NEW_ROOTS), paypalAccountName, UnknownAccount, Money("100"))
    )
    val retrieve = ApiAction.Transfer(organisationId(NEW_ROOTS), paypalAccountName, UnknownAccount, Money("100"))
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /transfer/organisationId should return BadRequest if insufficient funds") {
    val actions = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("50")),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), Bank, Money("0")),
      ApiAction.Transfer(organisationId(NEW_ROOTS), paypalAccountName, Bank, Money("100"))
    )
    val retrieve = ApiAction.Transfer(organisationId(NEW_ROOTS), paypalAccountName, Bank, Money("100"))
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.BadRequest)
  }

  test("POST /transfer/organisationId should return OK and update the organisation") {
    val actionsInit = Seq(
      ApiAction.AddOrganisation(NEW_ROOTS),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), paypalAccountName, Money("0")),
      ApiAction.AddAccount(organisationId(NEW_ROOTS), Bank, Money("0")),
    )
    val actionsDonate = Seq(
      ApiAction.AddDonation(organisationId(NEW_ROOTS), paypalAccountName, "Tom", "tom@gmail.com", Money("100"), None)
    )
    val actionsTransfer = Seq(
      ApiAction.Transfer(organisationId(NEW_ROOTS), paypalAccountName, Bank, Money("100"))
    )
    val retrieve = ApiAction.ListAccounts(organisationId(NEW_ROOTS))
    for
      _ <- ApiRun.apiRun(organisationRoute, actionsInit)
      _ <- ApiRun.apiRun(donationRoute, actionsDonate)
      _ <- ApiRun.apiRun(routes, actionsTransfer)
      res <- ApiRun.apiRun(organisationRoute, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      val accounts = res._2.get
      val total = accounts.map(_.balance).sum
      assertEquals(total, Money("100"))
      assertEquals(accounts.find(_.name == paypalAccountName).get.balance, Money.ZERO)
      assertEquals(accounts.find(_.name == Bank).get.balance, Money("100"))
  }

  test("POST /transfer/organisationId should handle invalid request body") {
    val invalidRequest = Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS)))
      .withEntity("{invalid json}")
    for
      resp <- routes.run(invalidRequest)
    yield assertEquals(resp.status, Status.BadRequest)
  }
