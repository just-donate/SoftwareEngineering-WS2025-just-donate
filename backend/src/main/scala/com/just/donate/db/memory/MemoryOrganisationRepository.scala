package com.just.donate.db.memory

import cats.effect.IO
import com.just.donate.models.Organisation

import scala.collection.mutable

case class MemoryOrganisationRepository(repository: mutable.Map[String, Organisation] = mutable.Map.empty)
    extends MemoryRepository[String, Organisation](repository):
  
  override def save(entity: Organisation): IO[Organisation] = IO {
    repository.put(entity.id, entity)
    entity
  }

  override def findAll(): IO[Seq[Organisation]] = IO {
    repository.values.toSeq
  }

  override def findById(id: String): IO[Option[Organisation]] = IO {
    repository.get(id)
  }

  override def delete(id: String): IO[Boolean] = IO {
    repository.remove(id).isDefined
  }
  
  override def update(entity: Organisation): IO[Organisation] = save(entity)
  
  def clear(): IO[Unit] = IO {
    repository.clear()
  }