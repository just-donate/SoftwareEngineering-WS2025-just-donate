//package com.just.donate.mocks.paypal
//
//import cats.effect.IO
//import com.just.donate.db.mongo.MongoPaypalRepository
//import com.just.donate.models.paypal.PayPalIPN
//import org.mongodb.scala.{Document, MongoCollection, MongoDatabase}
//
//class MongoPaypalRepositoryMock(collection: MongoCollection[Document]) extends MongoPaypalRepository(collection: MongoCollection[Document]) {
//  var stored: List[PayPalIPN] = PayPalIPN(payload="test") :: Nil
//
//  override def findAll(): IO[Seq[PayPalIPN]] = IO.pure(stored)
//
//  override def save(ipn: PayPalIPN): IO[PayPalIPN] = IO { 
//    stored = stored :+ ipn
//    ipn
//  }
//  
//  def reset(): Unit = stored = PayPalIPN(payload="test") :: Nil
//}
