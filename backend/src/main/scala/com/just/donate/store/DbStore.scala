package com.just.donate.store

import cats.effect.IO
import com.just.donate.models.Organisation

object DbStore extends Store:
  
  
  override def init(): Unit = println("DbStore init")

  override def save(id: String, organisation: Organisation): IO[Unit] = IO(println(s"DbStore save $id $organisation"))

  override def load(id: String): IO[Option[Organisation]] = IO(println(s"DbStore load $id")) *> IO.pure(None)

  override def list(): IO[List[String]] = IO(println("DbStore list")) *> IO.pure(List.empty)

  override def delete(id: String): IO[Unit] = IO(println(s"DbStore delete $id"))
