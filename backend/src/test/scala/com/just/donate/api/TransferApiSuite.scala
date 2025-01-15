package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.PaypalRoute.paypalAccountName
import com.just.donate.api.TransferRoute.RequestTransfer
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

class TransferApiSuite extends CatsEffectSuite:

  private val repo = MemoryOrganisationRepository()

  private val transferRoute = TransferRoute.transferRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound

  override def beforeEach(context: BeforeEach): Unit =
    val initRepo = for
      _ <- repo.clear()
      newRoots = createNewRoots()
      _ <- repo.save(newRoots)
    yield ()
    initRepo.unsafeRunSync()

  test("POST /transfer/organisationId should return OK and update the organisation") {
    val req =
      Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS)))
        .withEntity(RequestTransfer(paypalAccountName, "Bank", Money("100")))
    for
      _ <- addPaypalDonation(repo)
      resp <- transferRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.Ok)
      val updatedOrg = repo.findById(organisationId(NEW_ROOTS)).unsafeRunSync().get
      println(updatedOrg)
      assert(updatedOrg.totalBalance == Money("100"))
      assert(updatedOrg.getAccount(paypalAccountName).get.totalBalance == Money.ZERO)
      assert(updatedOrg.getAccount("Bank").get.totalBalance == Money("100"))
  }
