package com.just.donate.db.mongo

import cats.effect.IO
import com.just.donate.db.mongo.MongoRepository.ObservableOps
import com.just.donate.models.paypal.PayPalIPN
import com.just.donate.models.user.{Roles, User}
import org.mongodb.scala.*
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.*

/**
 * Implementation of CrudRepository for User.
 */
class MongoUserRepository(collection: MongoCollection[Document])
    extends MongoRepository[String, User](collection: MongoCollection[Document]):

  /**
   * Save a User instance into the collection.
   */
  override def save(user: User): IO[User] =
    val doc = Document(
      "email" -> user.email,
      "password" -> user.password,
      "role" -> user.role.toString, // Using toString to store the role as a string
      "active" -> user.active,
      "orgId" -> user.orgId
    )
    collection.insertOne(doc).toIO.map(_ => user)

  /**
   * Find all User documents.
   */
  override def findAll(): IO[Seq[User]] =
    collection.find().toIO.map { docs =>
      docs.toSeq.map(docToUser)
    }

  /**
   * Helper to convert a Document to a User case class.
   */
  private def docToUser(doc: Document): User =
    User(
      email = doc.getString("email"),
      password = doc.getString("password"),
      role = Roles.valueOf(doc.getString("role")), // Adjust according to your Roles implementation
      active = doc.getBoolean("active"),
      orgId = doc.getString("orgId")
    )

  /**
   * Find a single User document by its email.
   */
  override def findById(email: String): IO[Option[User]] =
    val filter = Filters.eq("email", email)
    collection.find(filter).first().toIO.map { docs =>
      docs.headOption.map(docToUser)
    }

  /**
   * Update an existing User document by email.
   */
  override def update(user: User): IO[User] =
    val filter = Filters.eq("email", user.email)
    val updateDoc = Updates.combine(
      Updates.set("email", user.email),
      Updates.set("password", user.password),
      Updates.set("role", user.role.toString),
      Updates.set("active", user.active),
      Updates.set("orgId", user.orgId)
    )
    collection.updateOne(filter, updateDoc).toIO.map(_ => user)

  /**
   * Delete a User document by its _id.
   */
  override def delete(email: String): IO[Boolean] =
    val filter = Filters.eq("email", email)
    collection.deleteOne(filter).toIO.map(_ => true)