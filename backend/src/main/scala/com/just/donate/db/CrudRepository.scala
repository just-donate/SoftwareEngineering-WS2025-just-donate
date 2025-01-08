package com.just.donate.db

import cats.effect.IO

/**
 * A repository for entities
 * @tparam E The type of entity (e.g. Organisation, Donation, etc.)
 * 28.12.2024
 * @see <a href="https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html">Spring Data CrudRepository</a>
 */
trait CrudRepository[E]:

  /**
   * Save a single entity
   * @param entity The entity to save
   */
  def save(entity: E): IO[Unit]

  /**
   * Save multiple entities
   * @param entities The entities to save
   * @return A no-result IO, signifying the side effect of saving the entities
   */
  def saveAll(entities: List[E]): IO[Unit]

  /**
   * Find all entities
   * @return A list of all entities
   */
  def findAll: IO[List[E]]

  /**
   * Find an entity by its ID
   * @param id The ID of the entity to find
   * @return The entity with the given ID, if it exists
   */
  def findById(id: String): IO[Option[E]]

  /**
   * Update an entity
   * @param id The ID of the entity to update
   * @param entity The new entity data
   * @return True if the entity was updated, false if it did not exist
   */
  def update(id: String, entity: E): IO[Boolean]

  /**
   * Delete an entity by its ID
   * @param id The ID of the entity to delete
   * @return A no-result IO, signifying the side effect of deleting the entity
   */
  def deleteById(id: String): IO[Unit]

  /**
   * Delete all entities
   * @return A no-result IO, signifying the side effect of deleting all entities
   */
  def deleteAll(): IO[Unit]
