package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.TransferRoute.RequestTransfer
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

class TransferApiSuite extends CatsEffectSuite:

  private val transferRoute = TransferRoute.transferRoute(MemoryStore, AppConfigMock(), EmailServiceMock()).orNotFound

  override def beforeEach(context: BeforeEach): Unit =
    MemoryStore.init()
    val newRoots = createNewRoots()
    MemoryStore.save(organisationId("newRoots"), newRoots).unsafeRunSync()

  test("POST /transfer/organisationId should return OK and update the organisation") {
    val req =
      Request[IO](Method.POST, testUri(organisationId("newRoots"))).withEntity(RequestTransfer("Paypal", "Bank", Money("100")))
    for
      _ <- addPaypalDonation
      resp <- transferRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.Ok)
      val updatedOrg = MemoryStore.load(organisationId("newRoots")).unsafeRunSync().get
      println(updatedOrg)
      assert(updatedOrg.totalBalance == Money("100"))
      assert(updatedOrg.getAccount("Paypal").get.totalBalance == Money.ZERO)
      assert(updatedOrg.getAccount("Bank").get.totalBalance == Money("100"))
  }
