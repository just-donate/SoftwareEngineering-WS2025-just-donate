package com.just.donate.models

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
  earmarking: Option[String] = None,
  id: String = UUID.randomUUID().toString
):

  override def toString: String = String.format("Donation from %s on %s", donorId, donationDate)

object Donation:

  def apply(donorId: String, amount: BigDecimal): DonationPart =
    apply(donorId, amount, LocalDateTime.now)

  def apply(donorId: String, amount: BigDecimal, donationDate: LocalDateTime): DonationPart =
    val donation = Donation(donorId, donationDate)
    DonationPart(amount, donation)

  def apply(donorId: String, amount: BigDecimal, earmarking: String): DonationPart =
    apply(donorId, amount, earmarking, LocalDateTime.now)

  def apply(donorId: String, amount: BigDecimal, earmarking: String, donationDate: LocalDateTime): DonationPart =
    val donation = Donation(donorId, donationDate, Some(earmarking))
    DonationPart(amount, donation)

