package com.just.donate.models

import com.just.donate.utils.Money


case class Expense(
  description: String,
  amount: Money,
  earMarking: Option[Earmarking] = None,
  paidBy: Seq[DonationPart] = Seq.empty
):

  def isBound: Boolean = earMarking.isDefined
