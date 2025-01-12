//package com.just.donate.db
//
//import cats.effect.IO
//import com.dimafeng.testcontainers.munit.TestContainerForAll
//import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
//import com.just.donate.db.mongo.MongoPaypalRepository
//import com.just.donate.models.paypal.PayPalIPN
//import munit.CatsEffectSuite
//import org.bson.types.ObjectId
//import org.mongodb.scala.MongoClient
//import org.testcontainers.containers.wait.strategy.Wait
//
//import java.io.File
//
//class MongoPaypalRepositorySuite extends CatsEffectSuite with TestContainerForAll:
//
//  /**
//   * Define the Docker Compose container for the Mongo database.
//   * @see <a href="https://github.com/testcontainers/testcontainers-scala/blob/master/docs/src/main/tut/usage.md">TestContainers Scala</a>
//   */
//  override val containerDef: DockerComposeContainer.Def =
//    DockerComposeContainer.Def(
//      new File("src/test/resources/db/docker-compose.yml"),
//      tailChildContainers = true,
//      exposedServices = Seq(
//        ExposedService(
//          "mongo_1",
//          27017,
//          Wait.forLogMessage(".*Waiting for connections.*", 1)
//        )
//      )
//    )
//
//  /**
//   * Test the connection to the Mongo container.
//   */
//  test("Should connect to the Mongo container") {
//    withContainers { mongo =>
//      // You can get the host/port from the container:
//      val host = mongo.getServiceHost("mongo_1", 27017)
//      val port = mongo.getServicePort("mongo_1", 27017)
//
//      // For example, create a real MongoClient
//      val connectionString = s"mongodb://$host:$port"
//      val client = org.mongodb.scala.MongoClient(connectionString)
//
//      val db = client.getDatabase("test-db")
//      val collection = db.getCollection("dummy")
//
//      assert(port != 0, s"Port must be mapped properly, found: $port")
//    }
//  }
//
//  /**
//   * Test the CRUD operations of the PaypalCrudRepository.
//   */
//  test("PaypalCrudRepository CRUD test") {
//    withContainers { mongo =>
//      val host = mongo.getServiceHost("mongo_1", 27017)
//      val port = mongo.getServicePort("mongo_1", 27017)
//      val connectionString = s"mongodb://$host:$port"
//      val client = MongoClient(connectionString)
//      val database = client.getDatabase("test-db")
//
//      val repo = new MongoPaypalRepository(database.getCollection("paypal_ipn"))
//
//      val collection = database.getCollection("paypal_ipn")
//      collection.drop()
//
//      val ipn1 = PayPalIPN(_id = ObjectId.get(), payload = "payload-1")
//      val ipn2 = PayPalIPN(_id = ObjectId.get(), payload = "payload-2")
//
//      val test = for
//        // save ipn1 and ipn2
//        _ <- repo.save(ipn1)
//        _ <- repo.save(ipn2)
//
//        // findAll should return both
//        all1 <- repo.findAll()
//        _ <- IO.println(s"DEBUG: final all2 = $all1")
//        _ = assertEquals(all1.size, 2, s"Expected 2 IPNs, got ${all1.size}")
//
//        // findById(ipn1._id) should be Some(ipn1)
//        found1 <- repo.findById(ipn1._id)
//        _ = assert(found1.isDefined, s"Expected to find IPN1 by ID, got None")
//        _ = assertEquals(found1.get.payload, "payload-1")
//
//        // update ipn2
//        updatedIpn2 = ipn2.copy(payload = "updated-2")
//        updatedOk <- repo.update(updatedIpn2)
//        _ = assertEquals(updatedOk.payload, "updated-2")
//        found2 <- repo.findById(ipn2._id)
//        _ = assertEquals(found2.get.payload, "updated-2")
//
//        // delete ipn1
//        _ <- repo.delete(ipn1._id)
//        all2 <- repo.findAll()
//        _ = assertEquals(all2.size, 1)
//      yield ()
//
//      test.unsafeRunSync()
//    }
//  }
