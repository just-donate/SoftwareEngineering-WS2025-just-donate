package com.just.donate.db.mongo

import cats.effect.IO
import com.just.donate.db.mongo.MongoRepository.ObservableOps
import com.just.donate.models.errors.ErrorLog
import org.bson.types.ObjectId
import org.mongodb.scala.*
import org.mongodb.scala.bson.*
import org.mongodb.scala.model.{Filters, Updates}

/**
 * A Mongo repository for ErrorLog records.
 *
 * This implementation extends a base MongoRepository and implements the required CRUD methods.
 */
class MongoErrorLogRepository(collection: MongoCollection[Document])
    extends MongoRepository[ObjectId, ErrorLog](collection):

  /**
   * Save an ErrorLog instance into the collection.
   *
   * If the error log's id is not defined, Mongo will generate one.
   */
  override def save(errorLog: ErrorLog): IO[ErrorLog] =
    val doc = Document(
      "errorType" -> errorLog.errorType,
      "message" -> errorLog.message,
      "rawData" -> errorLog.rawData,
      "timestamp" -> errorLog.timestamp.toString // store as ISO string, or use BSONDateTime if preferred
    )
    collection.insertOne(doc).toIO.map { _ =>
      // Optionally, you can extract the generated id and return the errorLog with id populated.
      errorLog
    }

  /**
   * Find all ErrorLog documents.
   */
  override def findAll(): IO[Seq[ErrorLog]] =
    collection.find().toIO.map { docs =>
      docs.toSeq.map(docToErrorLog)
    }

  /**
   * Find a single ErrorLog document by its id.
   */
  override def findById(id: ObjectId): IO[Option[ErrorLog]] =
    val filter = Filters.eq("_id", id)
    collection.find(filter).first().toIO.map { docs =>
      docs.headOption.map(docToErrorLog)
    }

  /**
   * Helper method to convert a Document to an ErrorLog instance.
   */
  private def docToErrorLog(doc: Document): ErrorLog =
    // Extract the _id as string and convert other fields
    val idOpt = doc.getObjectId("_id")
    val errorType = doc.getString("errorType")
    val message = doc.getString("message")
    val rawData = doc.getString("rawData")
    val timestampStr = doc.getString("timestamp")
    // You might want to add error handling for parsing the timestamp if required.
    val timestamp = java.time.Instant.parse(timestampStr)

    ErrorLog(idOpt, errorType, message, rawData, timestamp)

  /**
   * Update an existing ErrorLog document identified by its id.
   * (Usually logging entries are not updated; this is provided for completeness.)
   */
  override def update(errorLog: ErrorLog): IO[ErrorLog] =
    val filter = Filters.eq("_id", errorLog.id)
    val updateDoc = Updates.combine(
      Updates.set("errorType", errorLog.errorType),
      Updates.set("message", errorLog.message),
      Updates.set("rawData", errorLog.rawData),
      Updates.set("timestamp", errorLog.timestamp.toString)
    )
    collection.updateOne(filter, updateDoc).toIO.map(_ => errorLog)

  /**
   * Delete an ErrorLog document by its id.
   */
  override def delete(id: ObjectId): IO[Boolean] =
    val filter = Filters.eq("_id", id)
    collection.deleteOne(filter).toIO.map(_ => true)
