package com.just.donate.store

import cats.effect.IO
import com.just.donate.models.Organisation
import io.circe.generic.auto.*
import io.circe.jawn
import io.circe.syntax.*

import java.nio.file.{Files, Paths}

object FileStore extends Store:

  override def init(): Unit = Files.createDirectories(Paths.get("store"))

  override def save(id: String, organisation: Organisation): IO[Unit] =
    val json = organisation.asJson
    IO.blocking {
      Files.writeString(Paths.get(s"./store/$id.json"), json.spaces4)
    }

  override def load(id: String): IO[Option[Organisation]] =
    if Files.exists(Paths.get(s"store/$id.json")) then
      val json = Files.readString(Paths.get(s"store/$id.json"))
      IO(jawn.decode[Organisation](json).toOption)
    else
      IO.pure(None)

  override def list(): IO[List[String]] =
    val store = Paths.get("store")
    if Files.exists(store) then
      IO(Files.list(store).map(_.getFileName).toArray.map(_.toString.split('.').head).toList)
    else
      IO.pure(List.empty)

  override def delete(id: String): IO[Unit] =
    IO(Files.delete(Paths.get(s"store/$id.json")))