package com.just.donate.utils

import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.models.errors.ErrorLog
import org.mongodb.scala.bson.ObjectId

class ErrorLogger(errorLogRepo: Repository[ObjectId, ErrorLog]):

  /**
   * Logs an error with a specific type and returns an IO[Unit].
   *
   * @param errorType A string categorizing the error (e.g., "IPN", "DATABASE", "AUTHENTICATION")
   * @param message A message describing the error.
   * @param rawData The raw data relevant to the error (for instance, the raw request body).
   * @return IO[Unit]
   */
  def logError(errorType: String, message: String, rawData: String): IO[ErrorLog | Unit] =
    val errorLog = ErrorLog(
      errorType = errorType,
      message = message,
      rawData = rawData
    )
    errorLogRepo.save(errorLog).handleErrorWith { err =>
      // In case saving the error log fails, print to the console.
      IO.println(s"Failed to log error: ${err.getMessage}")
    }
