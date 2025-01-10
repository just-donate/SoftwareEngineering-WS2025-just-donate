package com.just.donate.mocks.paypal

import cats.effect.IO
import com.just.donate.db.PaypalRepository
import com.just.donate.models.PaypalIPN
import org.mongodb.scala.{Document, MongoCollection, MongoDatabase}

class PaypalRepositoryMock(collection: MongoCollection[Document]) extends PaypalRepository(collection: MongoCollection[Document]) {
  var stored: List[PaypalIPN] = PaypalIPN(payload="test") :: Nil

  override def findAll(): IO[Seq[PaypalIPN]] = IO.pure(stored)

  override def save(ipn: PaypalIPN): IO[PaypalIPN] = IO { 
    stored = stored :+ ipn
    ipn
  }
}
