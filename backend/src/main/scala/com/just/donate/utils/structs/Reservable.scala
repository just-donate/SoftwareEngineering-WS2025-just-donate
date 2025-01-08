package com.just.donate.utils.structs

import com.just.donate.models.DonationPart

case class Reservable(value: DonationPart, context: Option[String] = None):

  def isReserved: Boolean = context.isDefined

  def isReservedBy(ctx: String): Boolean = context.exists(c => c.equals(ctx))

  def reserve(ctx: String): Reservable = Reservable(value, Option(ctx))

  def release(): Reservable = Reservable(value, None)

  override def toString: String =
    if context.isDefined then String.format("%s(%s)", context.get, value)
    else value.toString
