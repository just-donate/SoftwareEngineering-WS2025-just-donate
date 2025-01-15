package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.DonationRoute.RequestDonation
import com.just.donate.api.PaypalRoute.paypalAccountName
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.helper.TestHelper.*
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import com.just.donate.utils.Money
import io.circe.generic.auto.*
import munit.{BeforeEach, CatsEffectSuite}
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
    val req = Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS), "account", paypalAccountName))
      .withEntity(RequestDonation("MyDonor", "mydonor@example.org", Money("100"), None))
    for
      resp <- donationRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.Ok)
      val updatedOrg = repo.findById(organisationId(NEW_ROOTS)).unsafeRunSync().get
      assert(updatedOrg.totalBalance == Money("100"))
  }

  test("POST /donate/organisationId/account/accountName should return BadRequest if organisation not found") {
    val req = Request[IO](Method.POST, testUri("unknown", "account", paypalAccountName))
      .withEntity(RequestDonation("MyDonor", "mydonor@example.org", Money("100"), None))
    for
      resp <- donationRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.BadRequest)
  }

  test("POST /donate/organisationId/account/accountName should return BadRequest if account not found") {
    val req = Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS), "unknown", paypalAccountName))
      .withEntity(RequestDonation("MyDonor", "mydonor@example.org", Money("100"), None))
    for
      resp <- donationRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.NotFound)
  }

  test("POST /donate/organisationId/account/accountName should return BadRequest if earmarking does not exist") {
    val req = Request[IO](Method.POST, testUri(organisationId(NEW_ROOTS), "account", paypalAccountName))
      .withEntity(RequestDonation("MyDonor", "mydonor@example.org", Money("100"), Some("unknown")))
    for
      resp <- donationRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.BadRequest)
  }
