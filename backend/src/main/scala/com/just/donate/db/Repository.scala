package com.just.donate.db

import cats.effect.IO

import scala.concurrent.Future

/**
 * A repository for entities
 * @tparam E The type of entity (e.g. Organisation, Donation, etc.)
 * 28.12.2024
 */
trait Repository[E]:
  /**
   * Create a new entity
   * @param entity The entity to create
   * @return A Future containing the ID of the created entity
   */
  def create(entity: E): IO[Unit]

  /**
   * Find all entities
   * @return A Future containing a list of all entities
   */
  def findAll: IO[List[E]]

  /**
   * Find an entity by ID
   * @param id The ID of the entity to find
   * @return A Future containing an Option of the entity
   */
  def findById(id: String): IO[Option[E]]

  /**
   * Update an entity by ID
   * @param id The ID of the entity to update
   * @param entity The entity to update
   * @return A Future containing a boolean indicating whether the update was successful
   */
  def update(id: String, entity: E): IO[Boolean]

  /**
   * Delete an entity by ID
   * @param id The ID of the entity to delete
   * @return A Future containing a boolean indicating whether the deletion was successful
   */
  def delete(id: String): IO[Boolean]
