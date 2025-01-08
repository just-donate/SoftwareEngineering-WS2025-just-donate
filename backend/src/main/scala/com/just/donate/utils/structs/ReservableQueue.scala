package com.just.donate.utils.structs

import com.just.donate.utils.Money
import com.just.donate.models.DonationPart

case class ReservableQueue(
  context: String,
  queue: Seq[Reservable] = Seq.empty[Reservable]
):

  def add(value: DonationPart): ReservableQueue = add(new Reservable(value))

  def add(value: Reservable): ReservableQueue =
    val toAdd = if value.isReservedBy(this.context) then value.release() else value
    ReservableQueue(context, queue.appended(toAdd))

  def pollUnreserved(): (Option[DonationPart], ReservableQueue) =
    val (polled, newQueue) = pollUnreserved(queue)
    (polled, ReservableQueue(context, newQueue))

  def pollUnreserved(s: Money, limit: Option[Int]): (Seq[DonationPart], Option[Money], ReservableQueue) =
    val (ts, ss, newQueue) = pollUnreserved(s, queue, limit)
    (ts, ss, ReservableQueue(context, newQueue))

  def peekUnreserved: Option[DonationPart] =
    queue.find(r => !r.isReserved).map(_.value)

  def hasUnreserved: Boolean =
    queue.exists(r => !r.isReserved)

  def isEmpty: Boolean =
    queue.isEmpty

  def isFullyReserved: Boolean =
    queue.forall(_.isReserved)

  def reserve(s: Money, context: String): (Option[Money], ReservableQueue) =
    val (ss, newQueue) = reserve(s, context, queue)
    (ss, ReservableQueue(context, newQueue))

  override def toString: String =
    "[" + queue.map(r => r.toString).zipWithIndex.map((s, i) => s"${i + 1}: $s").mkString(", ") + "]"

  private def pollUnreserved(inner: Seq[Reservable]): (Option[DonationPart], Seq[Reservable]) =
    inner match
      case Nil                              => (None, inner)
      case head :: tail if !head.isReserved => (Some(head.value), tail)
      case head :: tail =>
        val (polled, newQueue) = pollUnreserved(tail)
        (polled, head +: newQueue)

  private def pollUnreserved(
    s: Money,
    inner: Seq[Reservable],
    limit: Option[Int]
  ): (Seq[DonationPart], Option[Money], Seq[Reservable]) =
    limit match
      case Some(value) if value <= 0 => (Seq.empty, Some(s), inner)
      case _ =>
        inner match
          case Nil => (Seq.empty, Some(s), inner)
          case head +: tail if head.isReserved =>
            val (ts, ss, newQueue) = pollUnreserved(s, tail, limit)
            (ts, ss, head +: newQueue)
          case head +: tail =>
            head.value.splitOf(s) match
              // Nothing was split of, so we just return the head
              case Split(None, Some(remain), None) => (Seq.empty, None, Reservable(remain) +: tail)
              // Some was split off, so we return the split and the remain
              case Split(Some(split), Some(remain), None) => (Seq(split), None, Reservable(remain) +: tail)
              // Full split, so we return the split
              case Split(Some(split), None, None) => (Seq(split), None, tail)
              // Split of but remaining to split
              case Split(Some(split), None, Some(open)) =>
                val (ts, ss, newQueue) = pollUnreserved(open, tail, limit.map(_ - 1))
                (split +: ts, ss, newQueue)
              // Other cases should not happen
              case _ => throw new IllegalStateException("Should not happen?")

  private def reserve(s: Money, context: String, inner: Seq[Reservable]): (Option[Money], Seq[Reservable]) =
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
