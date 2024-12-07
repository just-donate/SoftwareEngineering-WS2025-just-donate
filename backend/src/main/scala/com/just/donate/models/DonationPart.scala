package com.just.donate.models

import com.just.donate.utils.Splittable
import com.just.donate.utils.Splittable.Split

import java.time.LocalDateTime

case class DonationPart(amount: BigDecimal, donationTime: LocalDateTime) 
  extends Splittable[DonationPart, BigDecimal] with Ordering[DonationPart]:

  override def splitOf(split: BigDecimal): Split[DonationPart, BigDecimal] =
    if split == BigDecimal(0) then
        Split(None, Some(this), None)
    else if split < amount then
        Split(Some(DonationPart(split, donationTime)), Some(DonationPart(amount - split, donationTime)), None)
    else if split == amount then
        Split(Some(this), None, None)
    else if split > amount then
        Split(Some(this), None, Some(split - amount))
    else
        throw new IllegalStateException("Should not happen?")

  override def toString: String = amount.toString

  override def compare(x: DonationPart, y: DonationPart): Int = x.donationTime.compareTo(y.donationTime)


