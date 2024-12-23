package com.just.donate.models

import org.scalatest.Assertions.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime

class DonationPartTest extends AnyFlatSpec with Matchers:

  "A DonationPart" should "correctly initialize with given amounts" in {
    val donationPart = Donation("Donor", BigDecimal(100))
    donationPart.amount shouldEqual BigDecimal(100)

    val donationPart2 = Donation("Donor", BigDecimal(0))
    donationPart2.amount shouldEqual BigDecimal.valueOf(0)

    val donationPart3 = Donation("Donor", BigDecimal(-100))
    donationPart3.amount shouldEqual BigDecimal.valueOf(-100)
  }

  it should "handle splitting with zero amount correctly" in {
    val donationPart = Donation("Donor", BigDecimal(100))
    val split = donationPart.splitOf(BigDecimal.valueOf(0))

    split.split shouldBe empty
    split.open shouldBe empty
    split.remain shouldBe defined
    split.remain.get.amount shouldEqual BigDecimal.valueOf(100)
  }

  it should "split a partial amount correctly" in {
    val donationPart = Donation("Donor", BigDecimal(100))
    val split = donationPart.splitOf(BigDecimal.valueOf(50))

    split.split shouldBe defined
    split.split.get.amount shouldEqual BigDecimal.valueOf(50)
    split.open shouldBe empty
    split.remain shouldBe defined
    split.remain.get.amount shouldEqual BigDecimal.valueOf(50)
  }

  it should "split the exact amount correctly" in {
    val donationPart = Donation("Donor", BigDecimal(100))
    val split = donationPart.splitOf(BigDecimal.valueOf(100))

    split.split shouldBe defined
    split.split.get.amount shouldEqual BigDecimal.valueOf(100)
    split.open shouldBe empty
    split.remain shouldBe empty
  }

  it should "handle splitting more than available amount correctly" in {
    val donationPart = Donation("Donor", BigDecimal(100))
    val split = donationPart.splitOf(BigDecimal.valueOf(150))

    split.split shouldBe defined
    split.split.get.amount shouldEqual BigDecimal.valueOf(100)
    split.open shouldBe defined
    split.open.get shouldEqual BigDecimal.valueOf(50)
  }

  it should "throw an IllegalStateException when splitting from an empty donation part" in {
    val donationPart = Donation("Donor", BigDecimal(0))
    an[IllegalStateException] should be thrownBy {
      donationPart.splitOf(BigDecimal.valueOf(100))
    }
  }
