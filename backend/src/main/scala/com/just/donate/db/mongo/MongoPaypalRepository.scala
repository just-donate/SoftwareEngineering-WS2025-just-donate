package com.just.donate.db.mongo

import cats.effect.IO
import com.just.donate.db.mongo.MongoRepository.ObservableOps
import com.just.donate.models.PaypalIPN
import org.mongodb.scala.*
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.*

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

/**
 * Implementation of CrudRepository for PayPal IPN.
 */
class MongoPaypalRepository(collection: MongoCollection[Document])
    extends MongoRepository[ObjectId, PaypalIPN](collection: MongoCollection[Document]):

  override def save(ipn: PaypalIPN): IO[PaypalIPN] =
    val doc = Document(
      "_id" -> ipn._id,
      "payload" -> ipn.payload
    )
    collection.insertOne(doc).toIO.map(_ => ipn)

  /**
   * Find all IPN documents
   */
  override def findAll(): IO[Seq[PaypalIPN]] =
    collection.find().toIO.map { docs =>
      docs.toSeq.map(docToPaypalIPN)
    }

  /**
   * Find a single IPN document by its _id (as String)
   */
  override def findById(id: ObjectId): IO[Option[PaypalIPN]] =
    val filter = Filters.eq("_id", id)
    collection.find(filter).first().toIO.map { docs =>
      // If the collection returns a single document, wrap it in Some
      // If none found, we get an empty sequence => None
      docs.headOption.map(docToPaypalIPN)
    }

  /**
   * Helper to convert a Document to a PaypalIPN case class
   */
  private def docToPaypalIPN(doc: Document): PaypalIPN =
    PaypalIPN(
      _id = doc.getObjectId("_id"),
      payload = doc.getString("payload")
    )

  /**
   * Update an existing IPN document by _id. Return true if something was updated.
   */
  override def update(ipn: PaypalIPN): IO[PaypalIPN] =
    val filter = Filters.eq("_id", ipn._id)
    val updateDoc = Updates.set("payload", ipn.payload)
    collection.updateOne(filter, updateDoc).toIO.map(_ => ipn)

  /**
   * Delete a document by its _id. Return true if something was deleted.
   */
  override def delete(id: ObjectId): IO[Boolean] =
    val filter = Filters.eq("_id", id)
    collection.deleteOne(filter).toIO.map(_ => true)
