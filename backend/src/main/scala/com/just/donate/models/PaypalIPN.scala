package com.just.donate.models

import org.mongodb.scala.bson.ObjectId

case class PaypalIPN
(
  _id: ObjectId = new ObjectId(),
  payload: String
)