package com.just.donate.utils

import scala.annotation.tailrec

object CollectionUtils:

  @tailrec
  private def updatedReturnHelper[T, R](seq: Seq[T], at: T => Boolean)(f: T => (R, T)): (Seq[T], Option[R]) =
    seq match
      case Seq() => (seq, None)
      case head +: tail if at(head) =>
        val (result, newHead) = f(head)
        (newHead +: tail, Some(result))
      case head +: tail => updatedReturnHelper(tail, at)(f)

  extension [T](seq: Seq[T])

    def updatedReturn[R](at: T => Boolean)(f: T => (R, T)): (Seq[T], Option[R]) =
      updatedReturnHelper(seq, at)(f)

    def updated(at: T => Boolean)(t: T): Seq[T] =
      updatedReturnHelper(seq, at)(_ => ((), t))._1

  extension [S, T](tuple: (S, T))
    def map1[R](f: S => R): (R, T) = (f(tuple._1), tuple._2)
    def map2[R](f: T => R): (S, R) = (tuple._1, f(tuple._2))
