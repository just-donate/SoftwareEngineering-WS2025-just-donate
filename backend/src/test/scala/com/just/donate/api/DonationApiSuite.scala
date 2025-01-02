package com.just.donate.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.just.donate.api.DonationRoute.RequestDonation
import com.just.donate.api.OrganisationRoute.ResponseOrganisation
import com.just.donate.helper.OrganisationHelper.createNewRoots
import com.just.donate.store.MemoryStore
import io.circe.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.implicits.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.implicits.*
import io.circe.*
import io.circe.generic.auto.*

class DonationApiSuite extends CatsEffectSuite:

  private val donationRoute = DonationRoute.donationRoute(MemoryStore).orNotFound
  private val organisationRoute = OrganisationRoute.organisationApi(MemoryStore).orNotFound

  override def beforeEach(context: BeforeEach): Unit =
    MemoryStore.init()
    val newRoots = createNewRoots()
    MemoryStore.save(organisationId("newRoots"), newRoots).unsafeRunSync()

  private def organisationId(name: String): String = name.hashCode.toString

  private def uri(paths: String*): Uri = Uri.unsafeFromString(paths.mkString("/"))

  test("GET /organisation/list should return the basic newRoots organisation in the beginning") {
    val req = Request[IO](Method.GET, uri"/list")
    for
      resp <- organisationRoute.run(req)
      status = resp.status
      body <- resp.as[List[String]]
    yield
      assertEquals(status, Status.Ok)
      assert(body.length == 1)
      assert(body.head == organisationId("newRoots"))
  }

  test("POST /donation/organisationId/account/accountName/donate should return OK and the updated organisation") {
    val req = Request[IO](Method.POST, uri(organisationId("newRoots"), "account", "Paypal", "donate"))
      .withEntity(RequestDonation("MyDonor", 100, None))
    for
      resp <- donationRoute.run(req)
      status = resp.status
    yield
      assertEquals(status, Status.Ok)
      val updatedOrg = MemoryStore.load(organisationId("newRoots")).unsafeRunSync().get
      println(updatedOrg)
      assert(updatedOrg.totalBalance == BigDecimal(100))
  }


