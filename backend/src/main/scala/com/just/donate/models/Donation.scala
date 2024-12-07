package com.just.donate.models

import java.time.LocalDateTime
import java.util

/**
 * Represents a single donation. A donation can be split into multiple parts, e.g. for different purposes.
 * The donation is always made by a donor and has a date when it was made.
 */
case class Donation(donor: String, donationDate: LocalDateTime, parts: Seq[DonationPart]):

  def this(donor: String, amount: BigDecimal, donationDate: LocalDateTime) =
    this(donor, donationDate, Seq(DonationPart(amount, donationDate)))

  def this(donor: String, amount: BigDecimal) =
    this(donor, amount, LocalDateTime.now)

  def getAmount: BigDecimal = parts.map(_.amount).sum

  override def toString: String = String.format("Donation from %s on %s", donor, donationDate)
