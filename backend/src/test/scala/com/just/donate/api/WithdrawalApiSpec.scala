package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.PaypalRoute.paypalAccountName
import com.just.donate.api.WithdrawalRoute.RequestWithdrawal
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.helper.TestHelper.*
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import com.just.donate.utils.Money
import io.circe.generic.auto.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

class WithdrawalApiSpec extends CatsEffectSuite:

  private val repo = MemoryOrganisationRepository()

  private val withdrawRoute =
    WithdrawalRoute.withdrawalRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound

  override def beforeEach(context: BeforeEach): Unit =
    val initRepo = for
      _ <- repo.clear()
      newRoots = createNewRoots()
      _ <- repo.save(newRoots)
    yield ()
    initRepo.unsafeRunSync()

  test("POST /withdraw/organisationId/account/accountName should return OK and update the organisation") {
    val req =
      Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS)))
        .withEntity(RequestWithdrawal(paypalAccountName, Money("100"), "test-description", None))
    for
      _ <- addPaypalDonation(repo)
      resp <- withdrawRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.Ok)
      val updatedOrg = repo.findById(organisationId(NEW_ROOTS)).unsafeRunSync().get
      println(updatedOrg)
      assert(updatedOrg.totalBalance == Money.ZERO)
      assert(updatedOrg.expenses.length == 1)
      assertEquals(updatedOrg.expenses.head.description, "test-description")
      assertEquals(updatedOrg.expenses.head.amount, Money("100"))
  }
