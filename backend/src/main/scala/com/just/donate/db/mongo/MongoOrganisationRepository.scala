package com.just.donate.db.mongo

import cats.effect.IO
import com.just.donate.db.mongo.MongoRepository.ObservableOps
import com.just.donate.models.Organisation
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.mongodb.scala.*
import org.mongodb.scala.model.*

class MongoOrganisationRepository(collection: MongoCollection[Document])
    extends MongoRepository[String, Organisation](collection):

  def findById(id: String): IO[Option[Organisation]] =
    collection
      .find(Filters.eq("_id", id))
      .first()
      .toIO
      .map(_.headOption.flatMap(doc => parse(doc.getString("data")).flatMap(_.as[Organisation]).fold(
        error => throw new Exception(error),
        organisation => Some(organisation)
      )))

  def findAll(): IO[Seq[Organisation]] =
    collection
      .find()
      .toIO
      .map(docs => docs.flatMap(doc => parse(doc.getString("data")).flatMap(_.as[Organisation]).toOption))

  def delete(id: String): IO[Boolean] =
    collection.deleteOne(Filters.eq("_id", id)).toIO.map(dr => dr.forall(_.wasAcknowledged()))

  override def update(entity: Organisation): IO[Organisation] =
    val doc = Document(
      "_id" -> entity.id,
      "data" -> entity.asJson.noSpaces
    )
    collection
      .replaceOne(
        Filters.eq("_id", entity.id),
        doc
      )
      .toIO
      .map(_ => entity)

  def save(organisation: Organisation): IO[Organisation] =
    val doc = Document(
      "_id" -> organisation.id,
      "data" -> organisation.asJson.noSpaces
    )
    collection
      .replaceOne(
        Filters.eq("_id", organisation.id),
        doc,
        ReplaceOptions().upsert(true)
      )
      .toIO
      .map(_ => organisation)
