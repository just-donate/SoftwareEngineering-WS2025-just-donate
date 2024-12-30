package com.just.donate.models

import java.time.LocalDateTime
import java.util
import java.util.UUID

/**
 * Represents a single donation. A donation can be split into multiple parts, e.g. for different purposes.
 * The donation is always made by a donor and has a date when it was made.
 */
case class Donation(
  donor: String,
  donationDate: LocalDateTime,
  earmarking: Option[String] = None,
  donorId: String = UUID.randomUUID().toString
):

  override def toString: String = String.format("Donation from %s on %s", donor, donationDate)

object Donation:

  def apply(donor: String, amount: BigDecimal): DonationPart =
    apply(donor, amount, LocalDateTime.now)

  def apply(donor: String, amount: BigDecimal, donationDate: LocalDateTime): DonationPart =
    val donation = Donation(donor, donationDate)
    DonationPart(amount, donation)

  def apply(donor: String, amount: BigDecimal, earmarking: String): DonationPart =
    apply(donor, amount, earmarking, LocalDateTime.now)

  def apply(donor: String, amount: BigDecimal, earmarking: String, donationDate: LocalDateTime): DonationPart =
    val donation = Donation(donor, donationDate, Some(earmarking))
    DonationPart(amount, donation)
