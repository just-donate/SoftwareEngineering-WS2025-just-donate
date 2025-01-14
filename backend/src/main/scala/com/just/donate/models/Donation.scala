package com.just.donate.models

import com.just.donate.utils.Money

import java.time.LocalDateTime
import java.util
import java.util.UUID

/**
 * Represents a single donation. A donation can be split into multiple parts, e.g. for different purposes.
 * The donation is always made by a donor and has a date when it was made.
 */
case class Donation(
  donorId: String,
  donationDate: LocalDateTime,
  amountRemaining: Money,
  amountTotal: Money,
  earmarking: Option[Earmarking] = None,
  id: String = UUID.randomUUID().toString,
  var statusUpdates: Seq[StatusUpdate] = Seq.empty
):

  override def toString: String = String.format("Donation from %s on %s", donorId, donationDate)

  def addStatusUpdate(status: StatusUpdate): Unit =
    statusUpdates = statusUpdates.appended(status)

object Donation:

  def apply(donorId: String, amount: Money): (Donation, DonationPart) =
    apply(donorId, amount, LocalDateTime.now)

  def apply(donorId: String, amount: Money, donationDate: LocalDateTime): (Donation, DonationPart) =
    val donation = Donation(donorId, donationDate, amount, amount)
    (donation, DonationPart(amount, donation.id, donationDate))

  def apply(donorId: String, amount: Money, earmarking: Earmarking): (Donation, DonationPart) =
    apply(donorId, amount, earmarking, LocalDateTime.now)

  def apply(
    donorId: String,
    amount: Money,
    earmarking: Earmarking,
    donationDate: LocalDateTime
  ): (Donation, DonationPart) =
    val donation = Donation(donorId, donationDate, amount, amount, Some(earmarking))
    (donation, DonationPart(amount, donation.id, donationDate))
