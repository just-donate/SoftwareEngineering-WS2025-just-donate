package com.just.donate.utils

import java.util.Optional

/**
 * A reservable wrapper around a value that can be split.
 *
 * @param R The type of the value, which must extend Splittable[R, S].
 * @param S The type used to split the value.
 * @param C The type of the context used for reservation.
 */
class Reservable[R <: Splittable[R, S], S, C](val value: R) {

  private var context: Optional[C] = Optional.empty()

  /**
   * Checks if the value is currently reserved.
   *
   * @return True if reserved, false otherwise.
   */
  def isReserved: Boolean = context.isPresent

  /**
   * Checks if the value is reserved by the specified context.
   *
   * @param ctx The context to check against.
   * @return True if reserved by the given context, false otherwise.
   */
  def isReservedBy(ctx: C): Boolean =
    context.map(c => c.equals(ctx)).orElse(false)

  /**
   * Reserves the value with the given context.
   *
   * @param ctx The context to reserve with.
   */
  def reserve(ctx: C): Unit = {
    this.context = Optional.of(ctx)
  }

  /**
   * Releases the reservation.
   */
  def release(): Unit = {
    this.context = Optional.empty()
  }

  /**
   * Retrieves the value.
   *
   * @return The wrapped value.
   */
  def getValue: R = value

  /**
   * Retrieves the current reservation context, if any.
   *
   * @return An Optional containing the context if reserved, otherwise empty.
   */
  def getContext: Optional[C] = context

  /**
   * Overrides the default hashCode method to match Java's behavior.
   *
   * @return The hash code of this instance.
   */
  override def hashCode(): Int = super.hashCode()

  /**
   * Overrides the default equals method to match Java's behavior.
   *
   * @param obj The object to compare with.
   * @return True if the objects are the same instance, false otherwise.
   */
  override def equals(obj: Any): Boolean = super.equals(obj)

  /**
   * Overrides the default toString method to provide a custom string representation.
   *
   * @return A string representation of this instance.
   */
  override def toString: String = {
    if (context.isPresent) {
      String.format("%s(%s)", context.get(), value)
    } else {
      value.toString
    }
  }
}