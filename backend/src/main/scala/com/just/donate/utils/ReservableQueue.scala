package com.just.donate.utils

case class ReservableQueue[T <: Splittable[T, S], S, C](
                                                         context: C,
                                                         queue: Seq[Reservable[T, S, C]] = Seq.empty[Reservable[T, S, C]]
                                                       ):

  def add(value: T): ReservableQueue[T, S, C] = add(new Reservable(value))

  def add(value: Reservable[T, S, C]): ReservableQueue[T, S, C] =
    val toAdd = if value.isReservedBy(this.context) then value.release() else value
    ReservableQueue(context, queue.appended(toAdd))

  def pollUnreserved(): (Option[T], ReservableQueue[T, S, C]) =
    val (polled, newQueue) = pollUnreserved(queue)
    (polled, ReservableQueue(context, newQueue))

  def pollUnreserved(s: S): (Seq[T], S, ReservableQueue[T, S, C]) =
    val (ts, ss, newQueue) = pollUnreserved(s, queue)
    (ts, ss, ReservableQueue(context, newQueue))

  def peekUnreserved: Option[T] =
    queue.find(r => !r.isReserved).map(_.value)

  def hasUnreserved: Boolean =
    queue.exists(r => !r.isReserved)

  def isEmpty: Boolean =
    queue.isEmpty

  def isFullyReserved: Boolean =
    queue.forall(_.isReserved)

  def reserve(s: S, context: C): (Option[S], ReservableQueue[T, S, C]) =
    val (ss, newQueue) = reserve(s, context, queue)
    (ss, ReservableQueue(context, newQueue))

  override def toString: String =
    "[" + queue.map(r => r.toString).zipWithIndex.map((s, i) => s"${i + 1}: $s").mkString(", ") + "]"

  private def pollUnreserved(inner: Seq[Reservable[T, S, C]]): (Option[T], Seq[Reservable[T, S, C]]) =
    inner match
      case Nil => (None, inner)
      case head :: tail if !head.isReserved => (Some(head.value), tail)
      case head :: tail =>
        val (polled, newQueue) = pollUnreserved(tail)
        (polled, head +: newQueue)

  private def pollUnreserved(s: S, inner: Seq[Reservable[T, S, C]]): (Seq[T], S, Seq[Reservable[T, S, C]]) =
    inner match
      case Nil => (Seq.empty, s, inner)
      case head +: tail if head.isReserved =>
        val (ts, ss, newQueue) = pollUnreserved(s, tail)
        (ts, ss, head +: newQueue)
      case head +: tail =>
        head.value.splitOf(s) match
          // Nothing was split of, so we just return the head
          case Split(None, Some(remain), None) => (Seq.empty, s, Reservable(remain) +: tail)
          // Some was split off, so we return the split and the remain
          case Split(Some(split), Some(remain), None) => (Seq(split), s, Reservable(remain) +: tail)
          // Full split, so we return the split
          case Split(Some(split), None, None) => (Seq(split), s, tail)
          // Split of but remaining to split
          case Split(Some(split), None, Some(open)) =>
            val (ts, ss, newQueue) = pollUnreserved(open, tail)
            (split +: ts, ss, newQueue)
          // Other cases should not happen
          case _ => throw new IllegalStateException("Should not happen?")

  private def reserve(s: S, context: C, inner: Seq[Reservable[T, S, C]]): (Option[S], Seq[Reservable[T, S, C]]) =
    inner match
      case Nil => (Some(s), inner)
      case head +: tail if head.isReserved =>
        val (ss, newQueue) = reserve(s, context, tail)
        (ss, head +: newQueue)
      case head +: tail =>
        head.value.splitOf(s) match
          // Nothing was split of, so we just return the head
          case Split(None, Some(remain), None) => (None, Reservable(remain) +: tail)
          // Some was split off, so we return the split and the remain
          case Split(Some(split), Some(remain), None) =>
            val splitOf = Reservable(split).reserve(context)
            (None, splitOf +: Reservable(remain) +: tail)
          // Full split, so we return the split
          case Split(Some(split), None, None) =>
            val splitOf = Reservable(split).reserve(context)
            (None, splitOf +: tail)
          // Split of but remaining to split
          case Split(Some(split), None, Some(open)) =>
            val (remS, remTail) = reserve(open, context, tail)
            (remS, Reservable(split).reserve(context) +: remTail)
          // Other cases should not happen
          case _ => throw new IllegalStateException("Should not happen?")
