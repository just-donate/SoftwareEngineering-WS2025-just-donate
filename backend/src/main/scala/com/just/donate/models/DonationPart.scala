package com.just.donate.models

import com.just.donate.models.Types.DonationGetter
import com.just.donate.utils.structs.{Split, Splittable}
import scala.math.Ordered.orderingToOrdered

import java.time.LocalDateTime
import com.just.donate.utils.Money
// TODO: when encoding and decoding json, make sure the connection between the donation and it's parts isn't lost
case class DonationPart(amount: Money, donationId: String, donationDate: LocalDateTime)
    extends Splittable[DonationPart, Money]
    with Ordering[DonationPart]:

  override def splitOf(split: Money): Split[DonationPart, Money] =
    if split == Money.ZERO then Split(None, Some(this), None)
    else if split < amount then
      Split(
        Some(DonationPart(split, donationId, donationDate)),
        Some(DonationPart(amount - split, donationId, donationDate)),
        None
      )
    else if split == amount then Split(Some(this), None, None)
    else if split > amount && amount > Money.ZERO then Split(Some(this), None, Some(split - amount))
    else throw new IllegalStateException("Should not happen?")

  override def toString: String = amount.toString

  override def compare(x: DonationPart, y: DonationPart): Int =
    x.donationDate.compareTo(y.donationDate)

  def donation(using donationGetter: DonationGetter): Option[Donation] =
    donationGetter(donationId)
  
  def earmarking(using donationGetter: DonationGetter): Option[String] =
    donationGetter(donationId).flatMap(_.earmarking)