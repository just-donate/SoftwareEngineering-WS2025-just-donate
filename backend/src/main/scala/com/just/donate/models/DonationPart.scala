package com.just.donate.models

import com.just.donate.utils.{Split, Splittable}

case class DonationPart(amount: BigDecimal, donation: Donation)
    extends Splittable[DonationPart, BigDecimal]
    with Ordering[DonationPart]:

  override def splitOf(split: BigDecimal): Split[DonationPart, BigDecimal] =
    if split == BigDecimal(0) then Split(None, Some(this), None)
    else if split < amount then
      Split(Some(DonationPart(split, donation)), Some(DonationPart(amount - split, donation)), None)
    else if split == amount then Split(Some(this), None, None)
    else if split > amount && amount > BigDecimal(0) then Split(Some(this), None, Some(split - amount))
    else throw new IllegalStateException("Should not happen?")

  override def toString: String = amount.toString

  override def compare(x: DonationPart, y: DonationPart): Int =
    x.donation.donationDate.compareTo(y.donation.donationDate)
