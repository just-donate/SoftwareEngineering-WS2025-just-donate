package com.just.donate.db.mongo

import cats.effect.IO
import com.just.donate.db.mongo.MongoRepository.ObservableOps
import com.just.donate.models.paypal.PayPalIPN
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.mongodb.scala.*
import org.mongodb.scala.model.*

/**
 * Implementation of CrudRepository for PayPal IPN.
 */
class MongoPaypalRepository(collection: org.mongodb.scala.MongoCollection[Document])
    extends MongoRepository[String, PayPalIPN](collection):

  override def save(ipn: PayPalIPN): IO[PayPalIPN] =
    val doc = Document(
      "_id" -> ipn.ipnTrackId,
      "data" -> ipn.asJson.noSpaces
    )
    collection
      .insertOne(
        doc
      )
      .toIO
      .map(_ => ipn)

  /**
   * Find all IPN documents
   */
  override def findAll(): IO[Seq[PayPalIPN]] =
    collection
      .find()
      .toIO
      .map(docs => docs.flatMap(doc => parse(doc.getString("data")).flatMap(_.as[PayPalIPN]).toOption))

  /**
   * Find a single IPN document by its _id (as ObjectId)
   */
  override def findById(id: String): IO[Option[PayPalIPN]] =
    collection
      .find(Filters.eq("_id", id))
      .first()
      .toIO
      .map(_.headOption.flatMap(doc => parse(doc.getString("data")).flatMap(_.as[PayPalIPN]).toOption))

  /**
   * Update an existing IPN document by _id.
   */
  override def update(ipn: PayPalIPN): IO[PayPalIPN] =
    val doc = Document(
      "_id" -> ipn.ipnTrackId,
      "data" -> ipn.asJson.noSpaces
    )
    collection
      .replaceOne(
        Filters.eq("_id", ipn.ipnTrackId),
        doc
      )
      .toIO
      .map(_ => ipn)

  /**
   * Delete a document by its _id.
   */
  override def delete(id: String): IO[Boolean] =
    collection.deleteOne(Filters.eq("_id", id)).toIO.map(dr => dr.forall(_.wasAcknowledged()))
