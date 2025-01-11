package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.DonationRoute.RequestDonation
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.helper.TestHelper.*
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import io.circe.generic.auto.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

import java.lang.Thread.sleep

class DonationApiSuite extends CatsEffectSuite:

  sleep(1); // Sleep for 1 second to avoid port conflict with other tests

  private val repo = MemoryOrganisationRepository()

  private val donationRoute =
    DonationRoute.donationRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound

  override def beforeEach(context: BeforeEach): Unit =
    val initRepo = for
      _ <- repo.clear()
      newRoots = createNewRoots()
      _ <- repo.save(newRoots)
    yield ()
    initRepo.unsafeRunSync()

  test("POST /donate/organisationId/account/accountName should return OK and update the organisation") {
    val req = Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS), "account", "Paypal"))
      .withEntity(RequestDonation("MyDonor", "mydonor@example.org", 100, None))
    for
      resp <- donationRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.Ok)
      val updatedOrg = repo.findById(organisationId(NEW_ROOTS)).unsafeRunSync().get
      println(">>>> ORG: " + updatedOrg)
      assert(updatedOrg.totalBalance == BigDecimal(100))
  }
