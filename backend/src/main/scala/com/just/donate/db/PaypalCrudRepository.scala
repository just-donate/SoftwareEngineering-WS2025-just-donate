package com.just.donate.db

import cats.effect.IO
import com.just.donate.models.PaypalIPN
import org.mongodb.scala.*
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.*

import scala.jdk.CollectionConverters.*

// Our extension to convert Mongo Observables into IO
object MongoOps:
  implicit class ObservableOps[T](val observable: Observable[T]) extends AnyVal:
    def toIO: IO[Seq[T]] = IO.fromFuture(IO(observable.toFuture()))

/**
 * Implementation of CrudRepository for PayPal IPN.
 */
class PaypalCrudRepository(database: MongoDatabase) extends CrudRepository[PaypalIPN]:

  import MongoOps.*

  // The collection name for PayPal IPN documents
  protected lazy val collection: MongoCollection[Document] = database.getCollection("paypal_ipn")

  /**
   * Insert a new IPN document
   */
  override def save(ipn: PaypalIPN): IO[Unit] =
    val doc = Document(
      "_id" -> ipn._id,
      "payload" -> ipn.payload
    )
    collection.insertOne(doc).toIO.map(_ => ())

  /**
   * Find all IPN documents
   */
  override def findAll: IO[List[PaypalIPN]] =
    collection.find().toIO.map { docs =>
      docs.toList.map(docToPaypalIPN)
    }

  /**
   * Helper to convert a Document to a PaypalIPN case class
   */
  private def docToPaypalIPN(doc: Document): PaypalIPN =
    PaypalIPN(
      // doc("_id") can be cast to ObjectId
      _id = doc.getObjectId("_id"),
      payload = doc.getString("payload")
    )

  /**
   * Find a single IPN document by its _id (as String)
   */
  override def findById(id: String): IO[Option[PaypalIPN]] =
    val filter = Filters.eq("_id", new ObjectId(id))
    collection.find(filter).first().toIO.map { docs =>
      // If the collection returns a single document, wrap it in Some
      // If none found, we get an empty sequence => None
      docs.headOption.map(docToPaypalIPN)
    }

  /**
   * Update an existing IPN document by _id. Return true if something was updated.
   */
  override def update(id: String, ipn: PaypalIPN): IO[Boolean] =
    val filter = Filters.eq("_id", new ObjectId(id))
    val updateDoc = Updates.set("payload", ipn.payload)
    collection.updateOne(filter, updateDoc).toIO.map { result =>
      result.nonEmpty
    }

  /**
   * Delete a document by its _id. Return true if something was deleted.
   */
  override def deleteById(id: String): IO[Unit] =
    val filter = Filters.eq("_id", new ObjectId(id))
    collection.deleteOne(filter).toIO.map { result =>
      result.nonEmpty
    }

  /**
   * Delete all IPN documents
   */
  override def deleteAll(): IO[Unit] = ???

  /**
   * Save multiple entities
   * @param entities The entities to save
   *  @return A no-result IO, signifying the side effect of saving the entities
   */
    override def saveAll(entities: List[PaypalIPN]): IO[Unit] = ???
