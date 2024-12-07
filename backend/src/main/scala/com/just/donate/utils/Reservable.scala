package com.just.donate.utils

case class Reservable[R <: Splittable[R, S], S, C](value: R, context: Option[C] = None):

  def isReserved: Boolean = context.isDefined

  def isReservedBy(ctx: C): Boolean = context.exists(c => c.equals(ctx))

  def reserve(ctx: C): Reservable[R, S, C] = Reservable(value, Option(ctx))

  def release(): Reservable[R, S, C] = Reservable(value, None)

  override def toString: String =
    if context.isDefined then String.format("%s(%s)", context.get, value)
    else value.toString
