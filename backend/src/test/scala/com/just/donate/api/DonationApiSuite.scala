package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.DonationRoute.RequestDonation
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.helper.TestHelper.*
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import com.just.donate.store.MemoryStore
import io.circe.generic.auto.*
import munit.{ BeforeEach, CatsEffectSuite }
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

class DonationApiSuite extends CatsEffectSuite:

  private val donationRoute = DonationRoute.donationRoute(MemoryStore, AppConfigMock(), EmailServiceMock()).orNotFound

  override def beforeEach(context: BeforeEach): Unit =
    MemoryStore.init()
    val newRoots = createNewRoots()
    MemoryStore.save(organisationId("newRoots"), newRoots).unsafeRunSync()

  test("POST /donate/organisationId/account/accountName should return OK and update the organisation") {
    val req = Request[IO](Method.POST, testUri(organisationId("newRoots"), "account", "Paypal"))
      .withEntity(RequestDonation("MyDonor", "mydonor@example.org", 100, None))
    for
      resp <- donationRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.Ok)
      val updatedOrg = MemoryStore.load(organisationId("newRoots")).unsafeRunSync().get
      println(updatedOrg)
      assert(updatedOrg.totalBalance == BigDecimal(100))
  }
