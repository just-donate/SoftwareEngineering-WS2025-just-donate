package com.just.donate.store

import cats.effect.IO
import com.just.donate.models.Organisation
import io.circe.generic.auto.*
import io.circe.jawn
import io.circe.syntax.*

import java.nio.file.{Files, Paths}

object FileStore extends Store:
  private val storePath = Paths.get("store")

  override def init(): Unit = Files.createDirectories(storePath)

  override def save(id: String, organisation: Organisation): IO[Unit] =
    val json = organisation.asJson
    IO.blocking {
      Files.writeString(storePathForId(id), json.spaces4)
    }

  override def load(id: String): IO[Option[Organisation]] =
    if Files.exists(storePathForId(id)) then
      val json = Files.readString(storePathForId(id))
      try
        val decoded = jawn.decode[Organisation](json)
        IO(decoded.toOption)
      catch
        // BUG: this triggers when donating the second time
        case e: Throwable =>
          println(f"encountered error while decoding: ${e}")
          throw e
    else IO.pure(None)

  private def storePathForId(id: String) = storePath.resolve(s"$id.json")

  override def list(): IO[List[String]] =
    if Files.exists(storePath) then
      IO(Files.list(storePath).map(_.getFileName).toArray.map(_.toString.split('.').head).toList)
    else IO.pure(List.empty)

  override def delete(id: String): IO[Unit] =
    IO(Files.delete(storePathForId(id)))
