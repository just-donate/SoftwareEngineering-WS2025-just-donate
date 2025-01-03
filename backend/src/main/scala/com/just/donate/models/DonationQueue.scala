package com.just.donate.models

import com.just.donate.utils.structs.ReservableQueue

case class DonationQueue(
  context: String,
  donationQueue: ReservableQueue,
  negativeBalance: BigDecimal = BigDecimal(0)
):

  def add(donation: DonationPart): DonationQueue =
    copy(donationQueue = donationQueue.add(donation))

  def addAll(donations: Seq[DonationPart]): DonationQueue =
    copy(donationQueue = donations.foldLeft(donationQueue)(_.add(_)))

  def totalBalance: BigDecimal = donationQueue.queue.map(_.value.amount).sum

  def pull(amount: BigDecimal, limit: Option[Int] = None): (Seq[DonationPart], DonationQueue) =
    val (donations, remaining, newQueue) = donationQueue.pollUnreserved(amount, limit)
    if remaining.isDefined && remaining.get > BigDecimal(0) then
      throw new IllegalArgumentException(s"Not enough donations to pull $amount (TODO: handle this)")
    (donations, copy(donationQueue = newQueue))