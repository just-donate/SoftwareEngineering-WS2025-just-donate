package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.WithdrawalRoute.RequestWithdrawal
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.helper.TestHelper.*
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import com.just.donate.store.MemoryStore
import com.just.donate.utils.Money
import io.circe.generic.auto.*
import munit.{BeforeEach, CatsEffectSuite}
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

class WithdrawlApiSpec extends CatsEffectSuite:

  private val withdrawRoute =
    WithdrawalRoute.withdrawalRoute(MemoryStore, AppConfigMock(), EmailServiceMock()).orNotFound

  override def beforeEach(context: BeforeEach): Unit =
    MemoryStore.init()
    val newRoots = createNewRoots()
    MemoryStore.save(organisationId("newRoots"), newRoots).unsafeRunSync()

  test("POST /withdraw/organisationId/account/accountName should return OK and update the organisation") {
    val req =
      Request[IO](Method.POST, testUri(organisationId("newRoots"), "account", "Paypal"))
        .withEntity(RequestWithdrawal(Money("100"), "test-description", None))
    for
      _ <- addPaypalDonation
      resp <- withdrawRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.Ok)
      val updatedOrg = MemoryStore.load(organisationId("newRoots")).unsafeRunSync().get
      println(updatedOrg)
      assert(updatedOrg.totalBalance == Money.ZERO)
      assert(updatedOrg.expenses.length == 1)
      assertEquals(updatedOrg.expenses.head.description, "test-description")
      assertEquals(updatedOrg.expenses.head.amount, Money("100"))
  }
