package com.just.donate.models.errors

import org.mongodb.scala.bson.ObjectId

import java.time.Instant

case class ErrorLog(
                     id: ObjectId = new ObjectId(), // Database-generated primary key.
                     errorType: String, // A string representing the type or category of the error.
                     message: String, // The error message/details.
                     rawData: String, // Store related data (e.g. the raw IPN body or request payload).
                     timestamp: Instant = Instant.now()
                   )
