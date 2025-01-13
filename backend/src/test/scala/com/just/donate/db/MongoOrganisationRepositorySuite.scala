package com.just.donate.db

import cats.effect.IO
import com.dimafeng.testcontainers.munit.TestContainerForAll
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import com.just.donate.api.PaypalRoute.paypalAccountName
import com.just.donate.db.mongo.MongoOrganisationRepository
import com.just.donate.helper.OrganisationHelper.createNewRoots
import com.just.donate.models.{Donation, Donor, Organisation}
import com.just.donate.utils.Money
import munit.CatsEffectSuite
import org.mongodb.scala.MongoClient
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File

class MongoOrganisationRepositorySuite extends CatsEffectSuite with TestContainerForAll:

  val testDb = "test-db"

  override val containerDef: DockerComposeContainer.Def =
    DockerComposeContainer.Def(
      new File("src/test/resources/db/docker-compose.yml"),
      tailChildContainers = true,
      exposedServices = Seq(
        ExposedService(
          "mongo_1",
          27017,
          Wait.forLogMessage(".*Waiting for connections.*", 1)
        )
      )
    )

  test("Should connect to the Mongo container") {
    withContainers { mongo =>
      val host = mongo.getServiceHost("mongo_1", 27017)
      val port = mongo.getServicePort("mongo_1", 27017)
      val connectionString = s"mongodb://$host:$port"
      val client = org.mongodb.scala.MongoClient(connectionString)

      val db = client.getDatabase(testDb)
      val collection = db.getCollection("dummy")

      assert(port != 0, s"Port must be mapped properly, found: $port")
    }
  }

  test("OrganisationRepository CRUD test") {
    withContainers { mongo =>
      val host = mongo.getServiceHost("mongo_1", 27017)
      val port = mongo.getServicePort("mongo_1", 27017)
      val connectionString = s"mongodb://$host:$port"
      val client = MongoClient(connectionString)
      val database = client.getDatabase(testDb)

      val repo = new MongoOrganisationRepository(database.getCollection("organisations"))

      val collection = database.getCollection("organisations")
      collection.drop()

      val org1 = createNewRoots()
      val org2 = Organisation("Another Org")

      val test = for
        // save org1 and org2
        _ <- repo.save(org1)
        _ <- repo.save(org2)

        // findAll should return both
        all1 <- repo.findAll()
        _ <- IO.println(s"DEBUG: all organisations = $all1")
        _ = assertEquals(all1.size, 2, s"Expected 2 organisations, got ${all1.size}")

        // findById(org1.id) should be Some(org1)
        found1 <- repo.findById(org1.id)
        _ = assert(found1.isDefined, s"Expected to find org1 by ID, got None")
        _ = assertEquals(found1.get.name, "New Roots")

        // update org2
        donor = Donor("1", "Donor", "donor@example.org")
        (donation, donationPart) = Donation(donor.id, Money("100"))
        updatedOrg2 = org2
          .addAccount("Bank")
          .addAccount(paypalAccountName)
          .donate(donor, donationPart, donation, paypalAccountName)
          .toOption
          .get
        updatedOk <- repo.update(updatedOrg2)
        _ = assertEquals(updatedOk.getAccount(paypalAccountName).get.name, paypalAccountName)
        _ = assertEquals(updatedOk.getAccount("Bank").get.name, "Bank")
        _ = assertEquals(updatedOk.totalBalance, Money("100"))
        found2 <- repo.findById(org2.id)
        _ = assertEquals(found2.get.getAccount(paypalAccountName).get.name, paypalAccountName)
        _ = assertEquals(found2.get.getAccount("Bank").get.name, "Bank")
        _ = assertEquals(found2.get.totalBalance, Money("100"))

        // delete org1
        _ <- repo.delete(org1.id)
        all2 <- repo.findAll()
        _ = assertEquals(all2.size, 1)
      yield ()

      test.unsafeRunSync()
    }
  }

  test("OrganisationRepository should handle accounts and earmarkings") {
    withContainers { mongo =>
      val host = mongo.getServiceHost("mongo_1", 27017)
      val port = mongo.getServicePort("mongo_1", 27017)
      val connectionString = s"mongodb://$host:$port"
      val client = MongoClient(connectionString)
      val database = client.getDatabase(testDb)

      val repo = new MongoOrganisationRepository(database.getCollection("organisations"))

      val collection = database.getCollection("organisations")
      collection.drop()

      var org = createNewRoots()
      org = org.addEarmarking("Education")

      val test = for
        // Save organisation with accounts and earmarking
        _ <- repo.save(org)

        // Retrieve and verify accounts and earmarkings
        found <- repo.findById(org.id)
        _ = assert(found.isDefined, "Expected to find organisation")
        _ = assertEquals(found.get.accounts.size, 4, "Expected 4 accounts")
        _ = assert(found.get.accounts.exists(_._2.name == paypalAccountName), "Expected Paypal account")
        _ = assert(found.get.accounts.exists(_._2.name == "Better Place"), "Expected Better Place account")
        _ = assert(found.get.accounts.exists(_._2.name == "Bank"), "Expected Bank account")
        _ = assert(found.get.accounts.exists(_._2.name == "Kenya"), "Expected Kenya account")
        _ = assert(found.get.getEarmarkings.contains("Education"), "Expected Education earmarking")
      yield ()

      test.unsafeRunSync()
    }
  }
