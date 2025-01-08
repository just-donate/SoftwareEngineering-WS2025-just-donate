package com.just.donate.mocks.paypal

import cats.effect.IO
import com.just.donate.db.PaypalCrudRepository
import com.just.donate.models.PaypalIPN
import org.mongodb.scala.{Document, MongoCollection, MongoDatabase}

class PaypalCrudRepositoryMock(database: MongoDatabase) extends PaypalCrudRepository(database: MongoDatabase) {
  var stored: List[PaypalIPN] = PaypalIPN(payload="test") :: Nil

  override lazy protected val collection: MongoCollection[Document] = ???

  override def findAll: IO[List[PaypalIPN]] =
    IO.pure(stored)

  override def save(ipn: PaypalIPN): IO[Unit] = IO { stored = stored :+ ipn }

  // Stub out any other methods if your trait has them:
  override def findById(id: String): IO[Option[PaypalIPN]] = ???
  override def update(id: String, ipn: PaypalIPN): IO[Boolean] = ???
  override def deleteById(id: String): IO[Unit] = ???
  override def deleteAll(): IO[Unit] = ???
  override def saveAll(entities: List[PaypalIPN]): IO[Unit] = ???
}
