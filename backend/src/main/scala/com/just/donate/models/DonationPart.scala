package com.just.donate.models

import com.just.donate.utils.structs.{ Split, Splittable }
import java.time.LocalDateTime

// TODO: when encoding and decoding json, make sure the connection between the donation and it's parts isn't lost
case class DonationPart(amount: BigDecimal, donationId: String, donationDate: LocalDateTime)
    extends Splittable[DonationPart, BigDecimal]
    with Ordering[DonationPart]:

  override def splitOf(split: BigDecimal): Split[DonationPart, BigDecimal] =
    if split == BigDecimal(0) then Split(None, Some(this), None)
    else if split < amount then
      Split(
        Some(DonationPart(split, donationId, donationDate)),
        Some(DonationPart(amount - split, donationId, donationDate)),
        None
      )
    else if split == amount then Split(Some(this), None, None)
    else if split > amount && amount > BigDecimal(0) then Split(Some(this), None, Some(split - amount))
    else throw new IllegalStateException("Should not happen?")

  override def toString: String = amount.toString

  override def compare(x: DonationPart, y: DonationPart): Int =
    x.donationDate.compareTo(y.donationDate)

  def donation(using donationGetter: DonationGetter) =
    donationGetter(donationId)
