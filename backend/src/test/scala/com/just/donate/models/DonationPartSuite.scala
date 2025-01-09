package com.just.donate.models

import com.just.donate.utils.Money
import munit.FunSuite

class DonationPartSuite extends FunSuite:

  test("correctly initialize with given amounts") {
    val (donation, donationPart) = Donation("Donor", Money("100"))
    assertEquals(donationPart.amount, Money("100"))

    val (donation2, donationPart2) = Donation("Donor", Money.ZERO)
    assertEquals(donationPart2.amount, Money.ZERO)

    val (donation3, donationPart3) = Donation("Donor", Money("-100"))
    assertEquals(donationPart3.amount, Money("-100"))
  }

  test("handle splitting with zero amount correctly") {
    val (donation, donationPart) = Donation("Donor", Money("100"))
    val split = donationPart.splitOf(Money.ZERO)

    assert(split.split.isEmpty, "There should be no split DonationPart for zero amount")
    assert(split.open.isEmpty, "There should be no 'open' leftover amount for zero split")
    assert(split.remain.isDefined, "There should be a remaining part")
    assertEquals(split.remain.get.amount, Money("100"))
  }

  test("split a partial amount correctly") {
    val (donation, donationPart) = Donation("Donor", Money("100"))
    val split = donationPart.splitOf(Money("50"))

    assert(split.split.isDefined, "There should be a split DonationPart")
    assertEquals(split.split.get.amount, Money("50"))
    assert(split.open.isEmpty, "No leftover beyond the split when it's within available amount")
    assert(split.remain.isDefined, "There should be a remaining part")
    assertEquals(split.remain.get.amount, Money("50"))
  }

  test("split the exact amount correctly") {
    val (donation, donationPart) = Donation("Donor", Money("100"))
    val split = donationPart.splitOf(Money("100"))

    assert(split.split.isDefined, "Splitting the exact amount should yield a new DonationPart")
    assertEquals(split.split.get.amount, Money("100"))
    assert(split.open.isEmpty, "No open leftover if the split amount is exactly available")
    assert(split.remain.isEmpty, "No remaining donation part if entire amount is split off")
  }

  test("handle splitting more than available amount correctly") {
    val (donation, donationPart) = Donation("Donor", Money("100"))
    val split = donationPart.splitOf(Money("150"))

    // The split portion is as large as the donation itself
    assert(split.split.isDefined, "Should still produce a DonationPart for the entire available amount")
    assertEquals(split.split.get.amount, Money("100"))

    // The 'open' leftover is the portion that can't be matched by the donation (150 - 100 = 50)
    assert(split.open.isDefined, "Should have an 'open' leftover amount")
    assertEquals(split.open.get, Money("50"))
  }

  test("throw an IllegalStateException when splitting from an empty donation part") {
    val (donation, donationPart) = Donation("Donor", Money.ZERO)
    intercept[IllegalStateException] {
      donationPart.splitOf(Money("100"))
    }
  }
