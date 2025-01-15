package com.just.donate.db

import cats.effect.IO
import com.dimafeng.testcontainers.munit.TestContainerForAll
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import com.just.donate.db.mongo.MongoPaypalRepository
import com.just.donate.models.paypal.PayPalIPN
import com.just.donate.utils.Money
import munit.CatsEffectSuite
import org.bson.types.ObjectId
import org.mongodb.scala.MongoClient
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File

class MongoPaypalRepositorySuite extends CatsEffectSuite with TestContainerForAll:

  /**
   * Define the Docker Compose container for the Mongo database.
   * @see <a href="https://github.com/testcontainers/testcontainers-scala/blob/master/docs/src/main/tut/usage.md">TestContainers Scala</a>
   */
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
  val updated2 = "updated=2"

  val samplePaypalTransaction1: PayPalIPN =
    PayPalIPN(
      txnId = "ABC123XYZ",
      txnType = "payment",
      paymentStatus = "Completed",
      paymentDate = "22:36:59 Jan 12, 2023 PST",
      mcGross = Money("100.00"),
      mcFee = Money("3.20"),
      invoice = Some("INV-999999"),
      payerId = "9XYT6ZABCD",
      payerEmail = "payer@example.com",
      payerStatus = "verified",
      firstName = "John",
      lastName = "Doe",
      notificationEmail = "notifications@example.com",
      receiverId = "RX12345ABC",
      receiverEmail = "receiver@example.com",
      business = "my-business@example.com",
      organisationName = "Just Donate Org",
      addressName = "John Doe",
      addressStreet = "1234 Elm Street",
      addressCity = "Metropolis",
      addressState = "NY",
      addressZip = "10001",
      addressCountry = "United States",
      addressCountryCode = "US",
      notifyVersion = "3.9",
      protectionEligibility = "Eligible",
      verifySign = "ABCDEF123456",
      ipnTrackId = "IPN-TRACK-XYZ",
      mcCurrency = "USD",
      quantity = 1,
      itemName = "Donation"
    )

  val samplePaypalTransaction2: PayPalIPN =
    PayPalIPN(
      txnId = "ABC123XYZ123",
      txnType = "payment",
      paymentStatus = "Completed",
      paymentDate = "22:36:59 Jan 12, 2023 PST",
      mcGross = Money("100.00"),
      mcFee = Money("3.20"),
      invoice = Some("INV-999999"),
      payerId = "9XYT6ZABCD",
      payerEmail = "payer2@example.com",
      payerStatus = "verified",
      firstName = "John",
      lastName = "Doe",
      notificationEmail = "notifications@example.com",
      receiverId = "RX12345ABC",
      receiverEmail = "receiver@example.com",
      business = "my-business@example.com",
      organisationName = "Just Donate Org",
      addressName = "John Doe",
      addressStreet = "1234 Elm Street",
      addressCity = "Metropolis",
      addressState = "NY",
      addressZip = "10001",
      addressCountry = "United States",
      addressCountryCode = "US",
      notifyVersion = "3.9",
      protectionEligibility = "Eligible",
      verifySign = "ABCDEF123456",
      ipnTrackId = "IPN-TRACK-XYZ",
      mcCurrency = "USD",
      quantity = 1,
      itemName = "Donation"
    )
  /**
   * Test the connection to the Mongo container.
   */
  test("Should connect to the Mongo container") {
    withContainers { mongo =>
      // You can get the host/port from the container:
      val host = mongo.getServiceHost("mongo_1", 27017)
      val port = mongo.getServicePort("mongo_1", 27017)

      // For example, create a real MongoClient
      val connectionString = s"mongodb://$host:$port"
      val client = org.mongodb.scala.MongoClient(connectionString)

      val db = client.getDatabase("test-db")
      val collection = db.getCollection("dummy")

      assert(port != 0, s"Port must be mapped properly, found: $port")
    }
  }

  /**
   * Test the CRUD operations of the PaypalCrudRepository.
   */
  test("PaypalCrudRepository CRUD test") {
    withContainers { mongo =>
      val host = mongo.getServiceHost("mongo_1", 27017)
      val port = mongo.getServicePort("mongo_1", 27017)
      val connectionString = s"mongodb://$host:$port"
      val client = MongoClient(connectionString)
      val database = client.getDatabase("test-db")

      val repo = new MongoPaypalRepository(database.getCollection("paypal_ipn"))

      val collection = database.getCollection("paypal_ipn")
      collection.drop()

      val ipn1 = samplePaypalTransaction1
      val ipn2 = samplePaypalTransaction2

      val test = for
        // save ipn1 and ipn2
        _ <- repo.save(ipn1)
        _ <- repo.save(ipn2)

        // findAll should return both
        all1 <- repo.findAll()
        _ <- IO.println(s"DEBUG: final all2 = $all1")
        _ = assertEquals(all1.size, 2, s"Expected 2 IPNs, got ${all1.size}")

        // findById(ipn1._id) should be Some(ipn1)
        found1 <- repo.findById(ipn1.ipnTrackId)
        _ = assert(found1.isDefined, s"Expected to find IPN1 by ID, got None")
        _ = assertEquals(found1.get.payerEmail, "payer@gmail.com")

        // update ipn2
        updatedIpn2 = ipn2.copy(payerEmail = updated2)
        updatedOk <- repo.update(updatedIpn2)
        _ = assertEquals(updatedOk.payerEmail, updated2)
        found2 <- repo.findById(ipn2.ipnTrackId)
        _ = assertEquals(found2.get.payerEmail, updated2)

        // delete ipn1
        _ <- repo.delete(ipn1.ipnTrackId)
        all2 <- repo.findAll()
        _ = assertEquals(all2.size, 1)
      yield ()

      test.unsafeRunSync()
    }
  }
