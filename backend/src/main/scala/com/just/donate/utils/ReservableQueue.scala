package com.just.donate.utils

import io.vavr.Tuple2
import io.vavr.collection.List
import io.vavr.control.Option

/**
 * A queue of reservable items that can be split based on a split value.
 *
 * @param T The type of the items in the queue, which must extend Splittable[T, S].
 * @param S The type used to split the items.
 * @param C The type of the context used for reservation.
 * @param context The context associated with this queue.
 */
class ReservableQueue[T <: Splittable[T, S], S, C](val context: C):

  private var queue: List[Reservable[T, S, C]] = List.empty()

  /**
   * Adds a new value to the queue.
   *
   * @param value The value to add.
   */
  def add(value: T): Unit =
    queue = queue.append(new Reservable[T, S, C](value))

  /**
   * Adds an existing Reservable to the queue. If the Reservable is reserved by this queue's context, it releases the reservation.
   *
   * @param value The Reservable to add.
   */
  def add(value: Reservable[T, S, C]): Unit =
    if value.isReservedBy(this.context) then value.release()
    queue = queue.append(value)

  /**
   * Retrieves the current queue.
   *
   * @return The list representing the queue.
   */
  def getQueue: List[Reservable[T, S, C]] = queue

  /**
   * Polls an unreserved item from the queue.
   *
   * @return An Option containing the polled item if available, otherwise none.
   */
  def pollUnreserved(): Option[T] =
    val head = queue.find(r => !r.isReserved).getOrNull
    if head == null then Option.none()
    else
      queue = queue.remove(head)
      Option.some(head.getValue)

  /**
   * Polls a specified amount from unreserved Reservables in the queue, splitting them if necessary.
   *
   * @param s The amount to poll.
   * @return A Tuple2 containing the list of polled items and the remaining amount not covered.
   */
  def pollUnreserved(s: S): Tuple2[List[T], S] =
    val peeked = queue.find(r => !r.isReserved)

    if peeked.isEmpty then new Tuple2(List.empty[T], s)
    else
      val head = peeked.get().getValue
      val split = head.splitOf(s)

      if split.fullRemain then new Tuple2(List.of(head), s)
      else if split.someSplit then
        queue = queue.replace(peeked.get(), new Reservable(split.getRemain().get()))
        new Tuple2(List.of(split.getSplit().get()), s)
      else if split.fullSplit then
        queue = queue.remove(peeked.get())
        new Tuple2(List.of(split.getSplit().get()), s)
      else if split.fullOpenSplit then
        queue = queue.remove(peeked.get())
        val polled = pollUnreserved(split.getOpen().get())
        new Tuple2(polled._1.prepend(split.getSplit().get()), polled._2)
      else throw new IllegalStateException("Should not happen?")

  /**
   * Peeks at the first unreserved item in the queue without removing it.
   *
   * @return An Option containing the first unreserved item if available, otherwise none.
   */
  def peekUnreserved(): Option[T] =
    queue.find(r => !r.isReserved).map(_.getValue)

  /**
   * Checks if there are any unreserved items in the queue.
   *
   * @return True if there are unreserved items, false otherwise.
   */
  def hasUnreserved(): Boolean =
    queue.exists(r => !r.isReserved)

  /**
   * Checks if the queue is empty.
   *
   * @return True if the queue is empty, false otherwise.
   */
  def isEmpty(): Boolean =
    queue.isEmpty

  /**
   * Checks if all items in the queue are fully reserved.
   *
   * @return True if all items are reserved, false otherwise.
   */
  def isFullyReserved(): Boolean =
    queue.forAll(_.isReserved)

  /**
   * Reserves a specified amount with the given context.
   *
   * @param s The amount to reserve.
   * @param context The context to reserve with.
   * @return The remaining amount after reservation.
   */
  def reserve(s: S, context: C): S =
    val res = reserveHelper(s, queue, context)
    queue = res._2
    res._1

  /**
   * Helper method for reserving a specified amount.
   *
   * @param s The amount to reserve.
   * @param values The current list of Reservables.
   * @param context The context to reserve with.
   * @return A Tuple2 containing the remaining amount and the updated list of Reservables.
   */
  private def reserveHelper(s: S, values: List[Reservable[T, S, C]], context: C): Tuple2[S, List[Reservable[T, S, C]]] =
    if values.isEmpty then new Tuple2(s, values)
    else
      val head = values.head()

      if head.isReserved then
        val res = reserveHelper(s, values.tail(), context)
        new Tuple2(res._1, res._2.prepend(head))
      else
        val split = head.getValue.splitOf(s)

        if split.fullRemain then new Tuple2(s, values.tail().prepend(head))
        else if split.someSplit then
          val splitOf = new Reservable(split.split.get())
          splitOf.reserve(context)
          new Tuple2(
            null.asInstanceOf[S],
            values.tail().prepend(new Reservable(split.remain.get())).prepend(splitOf)
          )
        else if split.fullSplit then
          val splitOf = new Reservable(split.split.get())
          splitOf.reserve(context)
          new Tuple2(null.asInstanceOf[S], values.tail().prepend(splitOf))
        else if split.fullOpenSplit then
          val splitOf = new Reservable(split.split.get())
          splitOf.reserve(context)
          val polled = reserveHelper(split.open.get(), values.tail(), context)
          new Tuple2(polled._1, polled._2.prepend(splitOf))
        else throw new IllegalStateException("Should not happen?")

  /**
   * Overrides the default toString method to provide a custom string representation of the queue.
   *
   * @return A string representation of the queue.
   */
  override def toString: String =
    "[" + queue.map(r => r.toString).zipWithIndex().map { case (str, idx) => s"${idx + 1}: $str" }.mkString(", ") + "]"
