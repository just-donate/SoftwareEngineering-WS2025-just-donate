package com.just.donate.models

import com.just.donate.utils.Money
import com.just.donate.utils.structs.ReservableQueue

import scala.math.Ordered.orderingToOrdered
case class DonationQueue(
  context: String,
  donationQueue: ReservableQueue,
  negativeBalance: Money = Money.ZERO
):

  def add(donation: DonationPart): DonationQueue =
    copy(donationQueue = donationQueue.add(donation))

  def addAll(donations: Seq[DonationPart]): DonationQueue =
    copy(donationQueue = donations.foldLeft(donationQueue)(_.add(_)))

  def totalBalance: Money = donationQueue.queue.map(_.value.amount).sum

  def pull(amount: Money, limit: Option[Int] = None): (Seq[DonationPart], Option[Money], DonationQueue) =
    val (donations, remaining, newQueue) = donationQueue.pollUnreserved(amount, limit)
    (donations, remaining, copy(donationQueue = newQueue))
