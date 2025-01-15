package com.just.donate.models

import com.just.donate.utils.Money

import java.time.LocalDateTime
import java.util.UUID

case class Expense(
  description: String,
  amount: Money,
  earMarking: Option[Earmarking] = None,
  paidBy: Seq[DonationPart] = Seq.empty,
  fromAccount: String,
  time: LocalDateTime = LocalDateTime.now,
  id: String = UUID.randomUUID().toString
):

  def isBound: Boolean = earMarking.isDefined
