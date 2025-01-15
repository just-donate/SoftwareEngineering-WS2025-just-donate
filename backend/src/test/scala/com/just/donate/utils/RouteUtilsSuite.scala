package com.just.donate.utils

import cats.effect.IO
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.models.*
import io.circe.generic.auto.*
import io.circe.{Encoder, Json}
import munit.CatsEffectSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

class RouteUtilsSuite extends CatsEffectSuite:
  private val repo = MemoryOrganisationRepository()
  private val organisation = Organisation("test-org")
  private val organisationId = organisation.id
  private val UnknownId = "unknown-id"

  override def beforeEach(context: BeforeEach): Unit = repo.clear().unsafeRunSync()

  // Helper method to create a complex organisation
  private def createComplexOrganisation(): Organisation =
    val org = organisation
      .addAccount("Account1")
      .addAccount("Account2")
      .addEarmarking(Earmarking("Education", "For education"))
      .addEarmarking(Earmarking("Health", "For health"))

    val donor = Donor(org.getNewDonorId, "Test Donor", "test@example.com")
    val (donation, donationPart) = Donation(donor.id, Money("100.0"))

    org.donate(donor, donationPart, donation, "Account1", com.just.donate.mocks.config.AppConfigMock()).toOption.get

  test("loadOrganisation should return NotFound for non-existent organisation") {
    val result =
      for response <- RouteUtils.loadOrganisation(UnknownId)(repo)(identity)
      yield response

    result.map { response =>
      assertEquals(response.status, Status.NotFound)
    }
  }

  test("loadOrganisation should return Ok with mapped organisation when found") {
    val result = for
      _ <- repo.save(organisation)
      response <- RouteUtils.loadOrganisation(organisationId)(repo)(_.id)
    yield response

    result.flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map(id => assertEquals(id, organisationId))
    }
  }

  test("loadOrganisation should handle complex data transformations") {
    val org = createComplexOrganisation()
    val result = for
      _ <- repo.save(org)
      response <- RouteUtils.loadOrganisation(organisationId)(repo) { org =>
        val i = org.accounts.map {
          case (name, account) =>
            (name, account.totalBalance, account.unboundDonations.totalBalance)
        }.toSeq
        i
      }
    yield response

    result.flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[Seq[(String, Money, Money)]].map { accounts =>
        assert(accounts.exists(_._1 == "Account1"))
        assert(accounts.exists(_._1 == "Account2"))
      }
    }
  }

  test("loadOrganisation should handle custom encoders") {
    given Encoder[Organisation] = (org: Organisation) =>
      Json.obj(
        "id" -> Json.fromString(org.id),
        "accountCount" -> Json.fromInt(org.accounts.size),
        "totalBalance" -> Json.fromString(org.totalBalance.toString)
      )

    val org = createComplexOrganisation()
    val result = for
      _ <- repo.save(organisation)
      response <- RouteUtils.loadOrganisation(organisationId)(repo)(identity)
    yield response

    result.map { response =>
      assertEquals(response.status, Status.Ok)
    }
  }

  test("loadAndSaveOrganisation should return NotFound for non-existent organisation") {
    val result =
      for response <- RouteUtils.loadAndSaveOrganisation(UnknownId)(repo)(identity)
      yield response

    result.map { response =>
      assertEquals(response.status, Status.NotFound)
    }
  }

  test("loadAndSaveOrganisation should handle complex updates") {
    val org = createComplexOrganisation()
    val result = for
      _ <- repo.save(organisation)
      response <- RouteUtils.loadAndSaveOrganisation(organisationId)(repo) { org =>
        org.addAccount("NewAccount").addEarmarking(Earmarking("NewEarmark", "New earmarking"))
      }
      updatedOrg <- repo.findById(organisationId)
    yield (response, updatedOrg)

    result.map {
      case (response, updatedOrg) =>
        assertEquals(response.status, Status.Ok)
        assert(updatedOrg.get.accounts.contains("NewAccount"))
        assert(updatedOrg.get.getEarmarking("NewEarmark").isDefined)
    }
  }

  test("loadOrganisationOps should return None for non-existent organisation") {
    val result =
      for response <- RouteUtils.loadOrganisationOps(UnknownId)(repo)(identity)
      yield response

    result.map { response =>
      assertEquals(response, None)
    }
  }

  test("loadOrganisationOps should return Some with mapped value when organisation exists") {
    val org = Organisation(organisationId)
    val result = for
      _ <- repo.save(organisation)
      response <- RouteUtils.loadOrganisationOps(organisationId)(repo)(_.id)
    yield response

    result.map { response =>
      assertEquals(response, Some(organisationId))
    }
  }

  test("loadOrganisationOps should handle complex data transformations") {
    val org = createComplexOrganisation()
    val result = for
      _ <- repo.save(org)
      response <- RouteUtils.loadOrganisationOps(organisationId)(repo) { org =>
        val totalBalance = org.totalBalance
        val accountBalances = org.accounts.view.mapValues(_.totalBalance).toMap
        val earmarkings = org.getEarmarkings
        (totalBalance, accountBalances, earmarkings)
      }
    yield response

    result.map { response =>
      assert(response.isDefined)
      val (totalBalance, accountBalances, earmarkings) = response.get
      assertEquals(totalBalance, Money("100.0"))
      assert(accountBalances.size == 2)
      assert(earmarkings.size == 2)
    }
  }

  test("loadAndSaveOrganisationOps should return None for non-existent organisation") {
    val result =
      for response <- RouteUtils.loadAndSaveOrganisationOps(UnknownId)(repo)(org => (org, org.id))
      yield response

    result.map { response =>
      assertEquals(response, None)
    }
  }

  test("loadAndSaveOrganisationOps should handle complex updates and transformations") {
    val org = createComplexOrganisation()
    val result = for
      _ <- repo.save(org)
      response <- RouteUtils.loadAndSaveOrganisationOps(organisationId)(repo) { org =>
        val updated = org.addAccount("NewAccount").addEarmarking(Earmarking("NewEarmark", "New earmarking"))

        val summary = (
          updated.accounts.size,
          updated.getEarmarkings.size,
          updated.totalBalance
        )
        (updated, summary)
      }
      updatedOrg <- repo.findById(organisationId)
    yield (response, updatedOrg)

    result.map {
      case (response, updatedOrg) =>
        assert(response.isDefined)
        val (accountCount, earmarkCount, totalBalance) = response.get
        assertEquals(accountCount, 3)
        assertEquals(earmarkCount, 3)
        assertEquals(totalBalance, Money("100.0"))
        assert(updatedOrg.get.accounts.contains("NewAccount"))
        assert(updatedOrg.get.getEarmarking("NewEarmark").isDefined)
    }
  }

  test("All operations should handle empty organisation data") {
    val emptyOrg = organisation
    val result = for
      _ <- repo.save(emptyOrg)
      loadResult <- RouteUtils.loadOrganisation(organisationId)(repo)(_.accounts.size)
      saveResult <- RouteUtils.loadAndSaveOrganisation(organisationId)(repo)(_.addAccount("NewAccount"))
      opsResult <- RouteUtils.loadOrganisationOps(organisationId)(repo)(_.accounts.isEmpty)
      saveOpsResult <- RouteUtils.loadAndSaveOrganisationOps(organisationId)(repo) { org =>
        val updated = org.addAccount("AnotherAccount")
        (updated, updated.accounts.size)
      }
    yield (loadResult, saveResult, opsResult, saveOpsResult)

    result.map {
      case (loadResult, saveResult, opsResult, saveOpsResult) =>
        assertEquals(loadResult.status, Status.Ok)
        assertEquals(saveResult.status, Status.Ok)
        assert(opsResult.contains(false))
        assert(saveOpsResult.contains(2))
    }
  }
