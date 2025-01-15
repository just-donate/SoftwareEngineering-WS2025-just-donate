package com.just.donate.utils

import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.models.errors.ErrorLog
import munit.CatsEffectSuite
import org.mongodb.scala.bson.ObjectId

class ErrorLoggerSuite extends CatsEffectSuite:
  // Mock repository that simulates successful saves
  private class SuccessfulMockRepo extends Repository[ObjectId, ErrorLog]:
    private var savedLogs = List.empty[ErrorLog]
    
    def clear(): IO[Unit] = IO { savedLogs = List.empty }
    def getLastSaved: IO[Option[ErrorLog]] = IO.pure(savedLogs.headOption)
    
    override def save(entity: ErrorLog): IO[ErrorLog] = 
      IO {
        savedLogs = entity :: savedLogs
        entity
      }
    
    override def findById(id: ObjectId): IO[Option[ErrorLog]] = IO.pure(None)
    override def findAll(): IO[List[ErrorLog]] = IO.pure(savedLogs)
    override def delete(id: ObjectId): IO[Boolean] = IO.pure(true)
    override def update(entity: ErrorLog): IO[ErrorLog] = IO.pure(entity)

  // Mock repository that simulates failed saves
  private class FailingMockRepo extends Repository[ObjectId, ErrorLog]:
    override def save(entity: ErrorLog): IO[ErrorLog] = 
      IO.raiseError(new RuntimeException("Simulated repository failure"))
    
    override def findById(id: ObjectId): IO[Option[ErrorLog]] = IO.pure(None)
    override def findAll(): IO[List[ErrorLog]] = IO.pure(List.empty)
    override def delete(id: ObjectId): IO[Boolean] = IO.pure(true)
    override def update(entity: ErrorLog): IO[ErrorLog] = IO.pure(entity)

  test("successfully log an error") {
    val mockRepo = SuccessfulMockRepo()
    val errorLogger = ErrorLogger(mockRepo)
    
    val result = for
      log <- errorLogger.logError("TEST", "Test error message", "Test raw data")
      saved <- mockRepo.getLastSaved
    yield (log, saved)
    
    result.map { case (log, savedOpt) =>
      assert(savedOpt.isDefined)
      val saved = savedOpt.get
      assertEquals(saved.errorType, "TEST")
      assertEquals(saved.message, "Test error message")
      assertEquals(saved.rawData, "Test raw data")
    }
  }

  test("handle repository failure gracefully") {
    val mockRepo = FailingMockRepo()
    val errorLogger = ErrorLogger(mockRepo)
    
    val result = errorLogger.logError("TEST", "Test message", "Test data")
    
    result.map { outcome =>
      assert(outcome.isInstanceOf[Unit])
    }
  }

  test("handle empty message and raw data") {
    val mockRepo = SuccessfulMockRepo()
    val errorLogger = ErrorLogger(mockRepo)
    
    val result = for
      log <- errorLogger.logError("TEST", "", "")
      saved <- mockRepo.getLastSaved
    yield saved
    
    result.map { savedOpt =>
      assert(savedOpt.isDefined)
      val saved = savedOpt.get
      assertEquals(saved.errorType, "TEST")
      assertEquals(saved.message, "")
      assertEquals(saved.rawData, "")
    }
  }

  test("handle special characters in message and raw data") {
    val mockRepo = SuccessfulMockRepo()
    val errorLogger = ErrorLogger(mockRepo)
    val specialMessage = "Test 🚀 message with special chars: !@#$%^&*()"
    val specialRawData = """{"key": "value with \n newline and \t tab"}"""
    
    val result = for
      log <- errorLogger.logError("TEST", specialMessage, specialRawData)
      saved <- mockRepo.getLastSaved
    yield saved
    
    result.map { savedOpt =>
      assert(savedOpt.isDefined)
      val saved = savedOpt.get
      assertEquals(saved.message, specialMessage)
      assertEquals(saved.rawData, specialRawData)
    }
  }

  test("handle very long message and raw data") {
    val mockRepo = SuccessfulMockRepo()
    val errorLogger = ErrorLogger(mockRepo)
    val longMessage = "a" * 1000
    val longRawData = "b" * 1000
    
    val result = for
      log <- errorLogger.logError("TEST", longMessage, longRawData)
      saved <- mockRepo.getLastSaved
    yield saved
    
    result.map { savedOpt =>
      assert(savedOpt.isDefined)
      val saved = savedOpt.get
      assertEquals(saved.message.length, 1000)
      assertEquals(saved.rawData.length, 1000)
    }
  }

  test("handle multiple error types") {
    val mockRepo = SuccessfulMockRepo()
    val errorLogger = ErrorLogger(mockRepo)
    
    val result = for
      _ <- errorLogger.logError("IPN", "IPN error", "ipn data")
      _ <- errorLogger.logError("DATABASE", "DB error", "sql error")
      _ <- errorLogger.logError("AUTHENTICATION", "Auth failed", "token data")
      logs <- mockRepo.findAll()
    yield logs
    
    result.map { logs =>
      assertEquals(logs.length, 3)
      assert(logs.exists(_.errorType == "IPN"))
      assert(logs.exists(_.errorType == "DATABASE"))
      assert(logs.exists(_.errorType == "AUTHENTICATION"))
    }
  } 