package com.just.donate.models

case class Expense(
  description: String,
  amount: BigDecimal,
  paidBy: Seq[DonationPart] = Seq.empty,
  earMarking: Option[String] = None
):

  def payWith(donationPart: DonationPart): Expense =
    if paidBy.contains(donationPart) then
      throw new IllegalStateException("Donation part already used to pay this expense")
    else copy(paidBy = paidBy :+ donationPart)

  def isPaid: Boolean = paidBy.map(_.amount).sum >= amount

  def isBound: Boolean = earMarking.isDefined
