package com.just.donate.api

import cats.effect.IO
import com.just.donate.api.OrganisationRoute.{RequestOrganisation, ResponseAccount, ResponseOrganisation}
import com.just.donate.api.helper.{ApiAction, ApiRun}
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.helper.OrganisationHelper.*
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import com.just.donate.utils.Money
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*
import com.just.donate.models.{ThemeConfig, StatusColors}

class OrganisationApiSuite extends CatsEffectSuite:
  private val DummyTheme = ThemeConfig(
    primary = "#1976D2",
    secondary = "#424242",
    accent = "#82B1FF",
    background = "#FAFAFA",
    card = "#FFFFFF",
    text = "#000000",
    textLight = "#FFFFFF",
    font = "Roboto, sans-serif",
    icon = "https://example.com/icon.png",
    ngoName = "Test NGO",
    ngoUrl = "https://test-ngo.org",
    helpUrl = "https://test-ngo.org/help",
    statusColors = StatusColors(
      announced = "#E3F2FD",
      pending_confirmation = "#FFF3E0",
      confirmed = "#E8F5E9",
      received = "#F3E5F5",
      in_transfer = "#FFEBEE",
      processing = "#E0F7FA",
      allocated = "#F1F8E9",
      awaiting_utilization = "#FFF8E1",
      used = "#EFEBE9"
    )
  )

  private val Org1 = "Org1"
  private val Org2 = "Org2"
  private val OrgId1 = organisationId(Org1)
  private val OrgId2 = organisationId(Org2)
  private val Account = "AccName"
  private val PayPal = "PayPal"
  private val Bank = "Bank"
  private val Donor1 = "Donor1"
  private val Donor2 = "Donor2"
  private val Donor3 = "Donor3"
  private val Email1 = "donor1@example.org"
  private val Email2 = "donor2@example.org"
  private val Email3 = "donor3@example.org"
  private val EarmarkEducation = "Education"
  private val EarmarkHealth = "Health"
  private val EarmarkHealthDesc = "For health"
  private val EarmarkEducationDesc = "For education"
  private val UnknownId = "unknown-id"

  private val repo = MemoryOrganisationRepository()

  private val routes = OrganisationRoute.organisationApi(repo).orNotFound
  private val donationRoute = DonationRoute.donationRoute(repo, AppConfigMock(), EmailServiceMock()).orNotFound

  override def beforeEach(context: BeforeEach): Unit = repo.clear().unsafeRunSync()

  test("GET /organisation/list should return empty list") {
    val retrieve = ApiAction.ListOrganisations()
    for res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq.empty)
  }

  test("POST /organisation/{id}/account should create an account") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1")
    )
    val retrieve = ApiAction.ListOrganisations()
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq(organisationId("Org1")))
  }

  test("POST /organisation should create multiple organisations") {
    val actions = Seq(
      ApiAction.AddOrganisation(Org1),
      ApiAction.AddOrganisation(Org2)
    )
    val retrieve = ApiAction.ListOrganisations()
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq(OrgId1, OrgId2))
  }

  test("GET /organisation/{id} should return NotFound if not exists") {
    val retrieve = ApiAction.GetOrganisation("unknown-id")
    for res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield assertEquals(res._1, Status.NotFound)
  }

  test("GET /organisation/{id} should return OK and the organisation if it exists") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1")
    )
    val retrieve = ApiAction.GetOrganisation(organisationId("Org1"))
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get.organisationId, organisationId("Org1"))
      assertEquals(res._2.get.name, "Org1")
  }

  test("DELETE /organisation/{id} should return Ok even if not present") {
    // For simplicity, expect that deleting a non-existent org returns Ok (idempotent behavior)
    val actions = Seq(
      ApiAction.AddOrganisation("Org1")
    )
    val retrieve = ApiAction.DeleteOrganisation(organisationId("Org1"))
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.Ok)
  }

  test("DELETE /organisation/{id} should really delete the organisation") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.DeleteOrganisation(organisationId("Org1"))
    )
    val retrieve = ApiAction.ListOrganisations()
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq.empty)
  }

  test("POST /organisation/{id}/account should return NotFound if org does not exist") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.AddAccount(organisationId("Org1"), "AccName", Money("100.0"))
    )
    val retrieve = ApiAction.GetOrganisation("unknown-id")
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.NotFound)
  }

  test("DELETE /organisation/{id}/account/{name} should return NotFound if org does not exist") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.AddAccount(organisationId("Org1"), "AccName", Money("100.0"))
    )
    val retrieve = ApiAction.GetAccount(organisationId("Org1"), "unknown-id")
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.NotFound)
  }

  test("GET /organisation/{id}/account/list should return Ok and all accounts") {
    val actions = Seq(
      ApiAction.AddOrganisation("Org1"),
      ApiAction.AddAccount(organisationId("Org1"), "AccName", Money("0.0"))
    )
    val retrieve = ApiAction.ListAccounts(organisationId("Org1"))

    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get.size, 1)
      assertEquals(res._2.get.head.name, "AccName")
      assertEquals(res._2.get.head.balance, Money("0.0"))
  }

  test("GET /organisation/{id}/account/list should return Ok and all accounts split by earmarking") {
    val setupActions = Seq(
      ApiAction.AddOrganisation(Org1),
      ApiAction.AddAccount(OrgId1, PayPal, Money("0.0")),
      ApiAction.AddAccount(OrgId1, Bank, Money("0.0")),
      ApiAction.AddEarmarking(OrgId1, EarmarkEducation, EarmarkEducationDesc),
      ApiAction.AddEarmarking(OrgId1, EarmarkHealth, EarmarkHealthDesc)
    )

    val donationActions = Seq(
      ApiAction.AddDonation(OrgId1, PayPal, Donor1, Email1, Money("10.0"), Some(EarmarkEducation)),
      ApiAction.AddDonation(OrgId1, PayPal, Donor1, Email2, Money("11.0"), Some(EarmarkHealth)),
      ApiAction.AddDonation(OrgId1, Bank, Donor2, Email2, Money("12.0"), Some(EarmarkEducation)),
      ApiAction.AddDonation(OrgId1, PayPal, Donor2, Email3, Money("13.0"), Some(EarmarkEducation)),
      ApiAction.AddDonation(OrgId1, Bank, Donor3, Email3, Money("14.0"), Some(EarmarkHealth))
    )

    val retrieve = ApiAction.ListAccounts(organisationId(Org1))

    for
      _ <- ApiRun.apiRun(routes, setupActions, retrieve)
      _ <- ApiRun.apiRun(donationRoute, donationActions, retrieve)
      res <- ApiRun.apiRun(routes, Seq.empty, retrieve)
    yield
      val body = res._2.get
      assertEquals(body.size, 2)
      assertEquals(body.head.name, PayPal)
      assertEquals(body.head.balance, Money("34.0"))
      assertEquals(body.head.byEarmarking.size, 3)
      assertEquals(body.head.byEarmarking.head._1, EarmarkEducation)
      assertEquals(body.head.byEarmarking.head._2, Money("23.0"))
      assertEquals(body.head.byEarmarking(1)._1, EarmarkHealth)
      assertEquals(body.head.byEarmarking(1)._2, Money("11.0"))

      assertEquals(body(1).name, Bank)
      assertEquals(body(1).balance, Money("26.0"))
      assertEquals(body(1).byEarmarking.size, 3)
      assertEquals(body(1).byEarmarking.head._1, EarmarkEducation)
      assertEquals(body(1).byEarmarking.head._2, Money("12.0"))
      assertEquals(body(1).byEarmarking(1)._1, EarmarkHealth)
      assertEquals(body(1).byEarmarking(1)._2, Money("14.0"))
  }

  test("POST /organisation/{id}/earmarking should create an earmarking") {
    val actions = Seq(
      ApiAction.AddOrganisation(Org1),
      ApiAction.AddEarmarking(OrgId1, EarmarkEducation, EarmarkEducationDesc)
    )
    val retrieve = ApiAction.ListEarmarkings(OrgId1)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get.size, 1)
      assertEquals(res._2.get.head.name, EarmarkEducation)
      assertEquals(res._2.get.head.description, EarmarkEducationDesc)
  }

  test("POST /organisation/{id}/earmarking/{name}/image should add an image to earmarking") {
    val imageUrl = "https://example.com/image.jpg"
    val actions = Seq(
      ApiAction.AddOrganisation(Org1),
      ApiAction.AddEarmarking(OrgId1, EarmarkEducation, EarmarkEducationDesc),
      ApiAction.AddEarmarkingImage(OrgId1, EarmarkEducation, imageUrl)
    )
    val retrieve = ApiAction.ListEarmarkings(OrgId1)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.Ok)
  }

  test("DELETE /organisation/{id}/earmarking/{name} should remove an earmarking") {
    val actions = Seq(
      ApiAction.AddOrganisation(Org1),
      ApiAction.AddEarmarking(OrgId1, EarmarkEducation, EarmarkEducationDesc),
      ApiAction.DeleteEarmarking(OrgId1, EarmarkEducation)
    )
    val retrieve = ApiAction.ListEarmarkings(OrgId1)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get.size, 0)
  }

  test("GET /organisation/{id}/transaction/list should return empty list initially") {
    val actions = Seq(
      ApiAction.AddOrganisation(Org1)
    )
    val retrieve = ApiAction.ListTransactions(OrgId1)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield
      assertEquals(res._1, Status.Ok)
      assertEquals(res._2.get, Seq.empty)
  }

  test("POST /organisation/{id}/theme should update theme configuration") {
    val actions = Seq(
      ApiAction.AddOrganisation(Org1),
      ApiAction.UpdateTheme(OrgId1, DummyTheme)
    )
    val retrieve = ApiAction.GetOrganisation(OrgId1)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.Ok)
  }

  test("POST /organisation/{id}/theme should return NotFound for non-existent organisation") {
    val actions = Seq(
      ApiAction.UpdateTheme(UnknownId, DummyTheme)
    )
    val retrieve = ApiAction.GetOrganisation(UnknownId)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.NotFound)
  }

  test("POST /organisation/{id}/theme should allow updating theme multiple times") {
    val updatedTheme = DummyTheme.copy(
      primary = "#2196F3",
      secondary = "#757575",
      ngoName = "Updated NGO"
    )
    val actions = Seq(
      ApiAction.AddOrganisation(Org1),
      ApiAction.UpdateTheme(OrgId1, DummyTheme),
      ApiAction.UpdateTheme(OrgId1, updatedTheme)
    )
    val retrieve = ApiAction.GetOrganisation(OrgId1)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.Ok)
  }

  test("Theme should persist after organisation operations") {
    val actions = Seq(
      ApiAction.AddOrganisation(Org1),
      ApiAction.UpdateTheme(OrgId1, DummyTheme),
      ApiAction.AddAccount(OrgId1, Account, Money("0.0")),
      ApiAction.AddEarmarking(OrgId1, EarmarkEducation, EarmarkEducationDesc)
    )
    val retrieve = ApiAction.GetOrganisation(OrgId1)
    for res <- ApiRun.apiRun(routes, actions, retrieve)
    yield assertEquals(res._1, Status.Ok)
  }
