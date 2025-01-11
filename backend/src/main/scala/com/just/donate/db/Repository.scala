package com.just.donate.db

import cats.effect.IO

/**
 * A generic Repository trait for basic CRUD operations.
 *
 * @tparam E the entity type
 * @tparam K the ID type of the entity
 */
trait Repository[K, E]:
  def findAll(): IO[Seq[E]]
  def findById(id: K): IO[Option[E]]
  def save(entity: E): IO[E]
  def update(entity: E): IO[E]
  def delete(id: K): IO[Boolean]
