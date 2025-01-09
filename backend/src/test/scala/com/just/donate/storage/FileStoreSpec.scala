package com.just.donate.storage

import com.just.donate.helper.{OrganisationDriver, OrganisationHelper}
import com.just.donate.models.Organisation
import munit.CatsEffectSuite

import java.nio.file.{Files, Path}

class FileStoreSpec extends CatsEffectSuite:

  val exampleOrg: Organisation = OrganisationHelper.createNewRoots()
  private val tempDir: Path = Files.createTempDirectory("store-test")

  override def beforeAll(): Unit =
    TestFileStore.init()

  object TestFileStore extends TestStore:
    override val storePath: Path = tempDir

  test("init should create the store directory if it doesn't exist") {
    assert(Files.exists(tempDir), s"Temporary directory $tempDir should exist")
  }

  test("save and load should properly persist and retrieve an Organisation") {
    val orgId = "test-org"

    for
      _ <- TestFileStore.save(orgId, exampleOrg) // Save the org
      loaded <- TestFileStore.load(orgId) // Load it back
    yield assertEquals(
      loaded,
      Some(exampleOrg),
      s"Loaded organisation should match the saved one for ID $orgId"
    )
  }

  test("list should return all IDs that have been saved") {
    val orgId1 = "newRoots"
    val orgId2 = "org2"
    val org1 = exampleOrg

    for
      org2 <- OrganisationDriver.createOrganisation(orgId2, "Organisation 2")
      _ <- TestFileStore.save(orgId1, org1)
      _ <- TestFileStore.save(orgId2, org2)
      ids <- TestFileStore.list()
    yield
      assert(ids.contains(orgId1), s"List should contain $orgId1")
      assert(ids.contains(orgId2), s"List should contain $orgId2")
  }

  test("delete should remove the file from the store") {
    val orgId = "delete-me"

    for
      org <- OrganisationDriver.createOrganisation(orgId, "To be deleted")
      _ <- TestFileStore.save(orgId, org)
      before <- TestFileStore.load(orgId)
      _ <- TestFileStore.delete(orgId)
      after <- TestFileStore.load(orgId)
    yield
      assert(before.isDefined, "Organisation should exist before deletion")
      assert(after.isEmpty, "Organisation should not exist after deletion")
  }
