package com.just.donate.store

import cats.effect.IO
import com.just.donate.models.Organisation

import scala.collection.mutable

case object MemoryStore extends Store:
   
  private val memoryStore: mutable.Map[String, Organisation] = mutable.HashMap()

  override def init(): Unit = memoryStore.clear()
  
  override def save(id: String, organisation: Organisation): IO[Unit] = for
    _ <- IO(memoryStore.put(id, organisation))
    unit <- IO.println(s"Saved organisation with id: $id, $organisation")
  yield unit

  override def load(id: String): IO[Option[Organisation]] = IO(memoryStore.get(id))

  override def list(): IO[List[String]] = IO(memoryStore.keys.toList)

  override def delete(id: String): IO[Unit] = IO(memoryStore.remove(id))

