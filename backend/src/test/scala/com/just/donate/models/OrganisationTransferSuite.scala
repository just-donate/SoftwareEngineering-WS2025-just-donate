package com.just.donate.models

import com.just.donate.helper.OrganisationHelper.createNewRoots
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.models.errors.TransferError
import com.just.donate.utils.Money
import munit.FunSuite

class OrganisationTransferSuite extends FunSuite:

  val donor1Email = "donor1@example.org"
  val amountHundred = "100.00"
  val amountOneFifty = "150.00"

  test("transfer money between accounts") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get
    newRoots = newRoots.transfer(Money("50.00"), "Paypal", "Bank", AppConfigMock()).toOption.get._1

    assertEquals(newRoots.getAccount("Paypal").get.totalBalance, Money("50.00"))
    assertEquals(newRoots.getAccount("Bank").get.totalBalance, Money("50.00"))
  }

  test("not transfer money if the source account does not have enough balance") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(Money(amountOneFifty), "Paypal", "Bank", AppConfigMock()),
      Left(TransferError.INSUFFICIENT_ACCOUNT_FUNDS)
    )
  }

  test("not transfer money if the source account does not exist") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(Money("50.00"), "NoExists", "Bank", AppConfigMock()),
      Left(TransferError.INVALID_ACCOUNT)
    )
  }

  test("not transfer money if the destination account does not exist") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(Money("50.00"), "Paypal", "NoExists", AppConfigMock()),
      Left(TransferError.INVALID_ACCOUNT)
    )
  }

  test("not transfer money if the amount is negative") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(Money("-50.00"), "Paypal", "Bank", AppConfigMock()),
      Left(TransferError.NON_POSITIVE_AMOUNT)
    )
  }

  test("not transfer money if the amount is zero") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(Money("0.00"), "Paypal", "Bank", AppConfigMock()),
      Left(TransferError.NON_POSITIVE_AMOUNT)
    )
  }

  test("not transfer money if the source account is the same as the destination account") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred))
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(Money("50.00"), "Paypal", "Paypal", AppConfigMock()),
      Left(TransferError.SAME_SOURCE_AND_DESTINATION_ACCOUNT)
    )
  }

  test("transfer earmarked money retains its earmarking after transfer") {
    var newRoots = createNewRoots()

    newRoots = newRoots.addEarmarking("Education")
    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money("200.00"), "Education")
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get
    newRoots = newRoots.transfer(Money(amountHundred), "Paypal", "Bank", AppConfigMock()).toOption.get._1

    assertEquals(newRoots.getAccount("Paypal").get.totalBalance, Money(amountHundred))
    assertEquals(newRoots.getAccount("Bank").get.totalBalance, Money(amountHundred))

    assertEquals(
      newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Education"),
      Money(amountHundred)
    )
    assertEquals(
      newRoots.getAccount("Bank").get.totalEarmarkedBalance("Education"),
      Money(amountHundred)
    )
  }

  test("transfers always the oldest donation if multiple are available") {
    var newRoots = createNewRoots()
    newRoots = newRoots.addEarmarking("Education")
    newRoots = newRoots.addEarmarking("Health")

    val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
    val (donation, donationPart) = Donation(donor.id, Money(amountHundred), "Education")
    newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get
    val donor2 = Donor(newRoots.getNewDonorId, "Donor2", "donor2@example.org")
    val (donation2, donationPart2) = Donation(donor2.id, Money(amountOneFifty), "Health")
    newRoots = newRoots.donate(donor2, donationPart2, donation2, "Paypal").toOption.get

    newRoots = newRoots.transfer(Money("50.00"), "Paypal", "Bank", AppConfigMock()).toOption.get._1

    assertEquals(newRoots.getAccount("Paypal").get.totalBalance, Money("200.00"))
    assertEquals(newRoots.getAccount("Bank").get.totalBalance, Money("50.00"))

    // Earmarked balances after partial transfer
    assertEquals(
      newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Education"),
      Money("50.00")
    )
    assertEquals(
      newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Health"),
      Money(amountOneFifty)
    )

    // The oldest donation (Education) is the one partially transferred
    assertEquals(
      newRoots.getAccount("Bank").get.totalEarmarkedBalance("Education"),
      Money("50.00")
    )
    assertEquals(
      newRoots.getAccount("Bank").get.totalEarmarkedBalance("Health"),
      Money("0.00")
    )
  }

  // FIX: this test
  // TODO: readd it afterwards
  // test("transfer multiple donation parts") {
  //   var newRoots = createNewRoots()
  //   newRoots = newRoots.addEarmarking("Education")
  //   newRoots = newRoots.addEarmarking("Health")
  //
  //   val donor = Donor(newRoots.getNewDonorId, "Donor1", donor1Email)
  //   val (donation, donationPart) = Donation(donor.id, Money(amountHundred), "Education")
  //   val (donation2, donationPart2) = Donation(donor.id, Money(amountOneFifty), "Education")
  //   newRoots = newRoots.donate(donor, donationPart, donation, "Paypal").toOption.get
  //   newRoots = newRoots.donate(donor, donationPart2, donation2, "Paypal").toOption.get
  //
  //   newRoots = newRoots.transfer(Money("120.00"), "Paypal", "Bank", AppConfigMock()).toOption.get._1
  //
  //   assertEquals(newRoots.getAccount("Paypal").get.totalBalance, Money("130.00"))
  //   assertEquals(newRoots.getAccount("Bank").get.totalBalance, Money("120.00"))
  //
  //   assertEquals(
  //     newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Education"),
  //     Money("130.00")
  //   )
  //   assertEquals(
  //     newRoots.getAccount("Bank").get.totalEarmarkedBalance("Education"),
  //     Money("120.00")
  //   )
  // }
