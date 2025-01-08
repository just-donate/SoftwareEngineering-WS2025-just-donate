package com.just.donate.models

case class Expense(
  description: String,
  amount: BigDecimal,
  earMarking: Option[String] = None,
  paidBy: Seq[DonationPart] = Seq.empty
):

  def isBound: Boolean = earMarking.isDefined
