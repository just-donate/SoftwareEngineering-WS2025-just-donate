package com.just.donate.mocks.paypal

import cats.effect.IO
import com.just.donate.db.mongo.MongoPaypalRepository
import com.just.donate.models.PaypalIPN
import org.mongodb.scala.{Document, MongoCollection, MongoDatabase}

class MongoPaypalRepositoryMock(collection: MongoCollection[Document]) extends MongoPaypalRepository(collection: MongoCollection[Document]) {
  var stored: List[PaypalIPN] = PaypalIPN(payload="test") :: Nil

  override def findAll(): IO[Seq[PaypalIPN]] = IO.pure(stored)

  override def save(ipn: PaypalIPN): IO[PaypalIPN] = IO { 
    stored = stored :+ ipn
    ipn
  }
  
  def reset(): Unit = stored = PaypalIPN(payload="test") :: Nil
}
