package com.just.donate.models

import com.just.donate.helper.OrganisationHelper.createNewRoots
import munit.FunSuite

import java.util.UUID

class OrganisationDonateSuite extends FunSuite:

  test("have a total balance of zero without any donations or expenses") {
    val newRoots = createNewRoots()
    assertEquals(newRoots.totalBalance, BigDecimal(0))
  }

  test("reflect unbound donations in the total balance") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val (donation, donationPart) = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail("Paypal account not found"))

    assertEquals(paypal.totalBalance, BigDecimal("100.00"))
    assertEquals(newRoots.totalBalance, BigDecimal("100.00"))
  }

  test("reflect bound donations in the total balance and earmarked balances") {
    var newRoots = createNewRoots()

    newRoots = newRoots.addEarmarking("Education")
    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val (donation, donationPart) = Donation(donor.id, BigDecimal("200.00"), "Education")
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail("Paypal account not found"))

    assertEquals(paypal.totalBalance, BigDecimal("200.00"))
    assertEquals(newRoots.totalBalance, BigDecimal("200.00"))

    assertEquals(paypal.totalEarmarkedBalance("Education"), BigDecimal("200.00"))
    assertEquals(newRoots.totalEarmarkedBalance("Education"), BigDecimal("200.00"))

    // TODO: Fix this test as it was commented out due to an error
    // intercept[IllegalStateException] {
    //   newRoots.totalEarmarkedBalance("Health")
    // }
  }

  test("aggregate donations from multiple accounts correctly in the total balance") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val (donation, donationPart) = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get
    val donor2 = Donor(newRoots.getNewDonorId, "Donor2", "donor2@example.org")
    val (donation2, donationPart2) = Donation(donor2.id, BigDecimal("150.00"))
    newRoots = newRoots.donate(donor2, donationPart2, donation2, "Bank").toOption.get

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail("Paypal account not found"))

    val bankOption = newRoots.getAccount("Bank")
    val bank = bankOption.getOrElse(fail("Bank account not found"))

    assertEquals(paypal.totalBalance, BigDecimal("100.00"))
    assertEquals(bank.totalBalance, BigDecimal("150.00"))
    assertEquals(newRoots.totalBalance, BigDecimal("250.00"))
  }

  test("correctly update the total balance when donations are made to accounts with incoming flows") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val (donation, donationPart) = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get
    val donor2 = Donor(newRoots.getNewDonorId, "Donor2", "donor2@example.org")
    val (donation2, donationPart2) = Donation(donor2.id, BigDecimal("150.00"))
    newRoots = newRoots.donate(donor2, donationPart2, donation2, "Bank").toOption.get

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail("Paypal account not found"))

    val bankOption = newRoots.getAccount("Bank")
    val bank = bankOption.getOrElse(fail("Bank account not found"))

    assertEquals(paypal.totalBalance, BigDecimal("100.00"))
    assertEquals(bank.totalBalance, BigDecimal("150.00"))
    assertEquals(newRoots.totalBalance, BigDecimal("250.00"))

    // Adding donation to an account with incoming flows
    val donor3 = Donor(newRoots.getNewDonorId, "Donor3", "donor3@example.org")
    val (donation3, donationPart3) = Donation(donor3.id, BigDecimal("200.00"))
    newRoots = newRoots.donate(donor3, donationPart3, donation3, "Kenya").toOption.get

    val kenyaOption = newRoots.getAccount("Kenya")
    val kenya = kenyaOption.getOrElse(fail("Kenya account not found"))

    assertEquals(kenya.totalBalance, BigDecimal("200.00"))
    assertEquals(newRoots.totalBalance, BigDecimal("450.00"))
  }
