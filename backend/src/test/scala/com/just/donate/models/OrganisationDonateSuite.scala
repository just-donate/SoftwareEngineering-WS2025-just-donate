package com.just.donate.models

import com.just.donate.helper.OrganisationHelper.createNewRoots
import com.just.donate.utils.Money
import munit.FunSuite

import java.util.UUID

class OrganisationDonateSuite extends FunSuite:
  
  val donor1Email = "donor1@example.org"
  val amountHundred = "100.00"
  val accountNotFoundError = "Paypal account not found"
  val amountTwoHundred = "200.00"
  val amountOneFifty = "150.00"

  test("have a total balance of zero without any donations or expenses") {
    val newRoots = createNewRoots()
    assertEquals(newRoots.totalBalance, Money.ZERO)
  }

  test("reflect unbound donations in the total balance") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail(accountNotFoundError))

    assertEquals(paypal.totalBalance, Money(amountHundred))
    assertEquals(newRoots.totalBalance, Money(amountHundred))
  }

  test("reflect bound donations in the total balance and earmarked balances") {
    var newRoots = createNewRoots()
    val educationEarmarking = Earmarking("Education", "Supporting education in Kenya")

    newRoots = newRoots.addEarmarking(educationEarmarking)
    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountTwoHundred), educationEarmarking)
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail(accountNotFoundError))

    assertEquals(paypal.totalBalance, Money(amountTwoHundred))
    assertEquals(newRoots.totalBalance, Money(amountTwoHundred))

    assertEquals(paypal.totalEarmarkedBalance(educationEarmarking), Money(amountTwoHundred))
    assertEquals(newRoots.totalEarmarkedBalance(educationEarmarking), Money(amountTwoHundred))

    // TODO: Fix this test as it was commented out due to an error
    // intercept[IllegalStateException] {
    //   newRoots.totalEarmarkedBalance("Health")
    // }
  }

  test("aggregate donations from multiple accounts correctly in the total balance") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get
    val donor2 = Donor(newRoots.getNewDonorId, "Donor2", "donor2@example.org")
    val (donation2, donationPart2) = Donation(donor2.id, Money(amountOneFifty))
    newRoots = newRoots.donate(donor2, donationPart2, donation2, "Bank").toOption.get

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail(accountNotFoundError))

    val bankOption = newRoots.getAccount("Bank")
    val bank = bankOption.getOrElse(fail("Bank account not found"))

    assertEquals(paypal.totalBalance, Money(amountHundred))
    assertEquals(bank.totalBalance, Money(amountOneFifty))
    assertEquals(newRoots.totalBalance, Money("250.00"))
  }

  test("correctly update the total balance when donations are made to accounts with incoming flows") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get
    val donor2 = Donor(newRoots.getNewDonorId, "Donor2", "donor2@example.org")
    val (donation2, donationPart2) = Donation(donor2.id, Money(amountOneFifty))
    newRoots = newRoots.donate(donor2, donationPart2, donation2, "Bank").toOption.get

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail(accountNotFoundError))

    val bankOption = newRoots.getAccount("Bank")
    val bank = bankOption.getOrElse(fail("Bank account not found"))

    assertEquals(paypal.totalBalance, Money(amountHundred))
    assertEquals(bank.totalBalance, Money(amountOneFifty))
    assertEquals(newRoots.totalBalance, Money("250.00"))

    // Adding donation to an account with incoming flows
    val donor3 = Donor(newRoots.getNewDonorId, "Donor3", "donor3@example.org")
    val (donation3, donationPart3) = Donation(donor3.id, Money(amountTwoHundred))
    newRoots = newRoots.donate(donor3, donationPart3, donation3, "Kenya").toOption.get

    val kenyaOption = newRoots.getAccount("Kenya")
    val kenya = kenyaOption.getOrElse(fail("Kenya account not found"))

    assertEquals(kenya.totalBalance, Money(amountTwoHundred))
    assertEquals(newRoots.totalBalance, Money("450.00"))
  }
