package com.just.donate.storage

import cats.effect.{IO, Sync}
import com.just.donate.models.Organisation
import com.just.donate.store.Store
import io.circe.generic.auto.*
import io.circe.jawn
import io.circe.syntax.*

import java.nio.file.{Files, Path}
import scala.jdk.StreamConverters.*

trait TestStore extends Store {
  def storePath: Path

  override def init(): Unit =
    Files.createDirectories(storePath)

  override def save(id: String, organisation: Organisation): IO[Unit] =
    val json = organisation.asJson
    IO.blocking {
      Files.writeString(storePathForId(id), json.spaces4)
    }

  override def load(id: String): IO[Option[Organisation]] =
    val path = storePathForId(id)
    if Files.exists(path) then
      IO.blocking(Files.readString(path)).flatMap { json =>
        IO {
          jawn.decode[Organisation](json).toOption
        }.handleErrorWith { e =>
          // Logging for demonstration:
          IO.println(s"encountered error while decoding: $e") *> IO.raiseError(e)
        }
      }
    else IO.pure(None)

  private def storePathForId(id: String) =
    storePath.resolve(s"$id.json")

  override def list(): IO[List[String]] =
    IO.blocking {
      if Files.exists(storePath) then
        Files
          .list(storePath)
          .toScala(Iterator)
          .map { p =>
            val filename = p.getFileName.toString
            filename.split("\\.").headOption.getOrElse(filename)
          }
          .toList
      else
        List.empty
    }

  override def delete(id: String): IO[Unit] =
    IO.blocking {
      Files.deleteIfExists(storePathForId(id))
    }
}
