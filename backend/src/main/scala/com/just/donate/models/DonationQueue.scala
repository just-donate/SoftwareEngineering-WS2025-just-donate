package com.just.donate.models

import com.just.donate.utils.ReservableQueue

case class DonationQueue(
  context: Account,
  donationQueue: ReservableQueue[DonationPart, BigDecimal, Account],
  negativeBalance: BigDecimal = BigDecimal(0)
):

  def add(donation: DonationPart): DonationQueue =
    copy(donationQueue = donationQueue.add(donation))

  def addAll(donations: Seq[DonationPart]): DonationQueue =
    copy(donationQueue = donations.foldLeft(donationQueue)(_.add(_)))

  def totalBalance: BigDecimal = donationQueue.queue.map(_.value.amount).sum
