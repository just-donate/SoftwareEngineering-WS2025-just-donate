package com.just.donate.db

import cats.effect.IO

/**
 * A generic Repository trait for basic CRUD operations.
 *
 * @tparam T the entity type
 * @tparam K the ID type of the entity
 */
trait Repository[T, K] {
  def findAll(): IO[Seq[T]]
  def findById(id: K): IO[Option[T]]
  def save(entity: T): IO[T]
  def update(entity: T): IO[T]
  def delete(id: K): IO[Boolean]
}
