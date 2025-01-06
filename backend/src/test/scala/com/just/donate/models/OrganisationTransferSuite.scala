package com.just.donate.models

import com.just.donate.helper.OrganisationHelper.createNewRoots
import munit.FunSuite
import com.just.donate.mocks.config.AppConfigMock

class OrganisationTransferSuite extends FunSuite:

  test("transfer money between accounts") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get
    newRoots = newRoots.transfer(BigDecimal("50.00"), "Paypal", "Bank", AppConfigMock()).toOption.get._1

    assertEquals(newRoots.getAccount("Paypal").get.totalBalance, BigDecimal("50.00"))
    assertEquals(newRoots.getAccount("Bank").get.totalBalance, BigDecimal("50.00"))
  }

  test("not transfer money if the source account does not have enough balance") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(BigDecimal("150.00"), "Paypal", "Bank", AppConfigMock()),
      Left(TransferError.INSUFFICIENT_ACCOUNT_FUNDS)
    )
  }

  test("not transfer money if the source account does not exist") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(BigDecimal("50.00"), "NoExists", "Bank", AppConfigMock()),
      Left(TransferError.INVALID_ACCOUNT)
    )
  }

  test("not transfer money if the destination account does not exist") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(BigDecimal("50.00"), "Paypal", "NoExists", AppConfigMock()),
      Left(TransferError.INVALID_ACCOUNT)
    )
  }

  test("not transfer money if the amount is negative") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(BigDecimal("-50.00"), "Paypal", "Bank", AppConfigMock()),
      Left(TransferError.NON_POSITIVE_AMOUNT)
    )
  }

  test("not transfer money if the amount is zero") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(BigDecimal("0.00"), "Paypal", "Bank", AppConfigMock()),
      Left(TransferError.NON_POSITIVE_AMOUNT)
    )
  }

  test("not transfer money if the source account is the same as the destination account") {
    var newRoots = createNewRoots()

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("100.00"))
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get

    assertEquals(
      newRoots.transfer(BigDecimal("50.00"), "Paypal", "Paypal", AppConfigMock()),
      Left(TransferError.SAME_SOURCE_AND_DESTINATION_ACCOUNT)
    )
  }

  test("transfer earmarked money retains its earmarking after transfer") {
    var newRoots = createNewRoots()

    newRoots = newRoots.addEarmarking("Education")
    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("200.00"), "Education")
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get
    newRoots = newRoots.transfer(BigDecimal("100.00"), "Paypal", "Bank", AppConfigMock()).toOption.get._1

    assertEquals(newRoots.getAccount("Paypal").get.totalBalance, BigDecimal("100.00"))
    assertEquals(newRoots.getAccount("Bank").get.totalBalance, BigDecimal("100.00"))

    assertEquals(
      newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Education"),
      BigDecimal("100.00")
    )
    assertEquals(
      newRoots.getAccount("Bank").get.totalEarmarkedBalance("Education"),
      BigDecimal("100.00")
    )
  }

  test("transfers always the oldest donation if multiple are available") {
    var newRoots = createNewRoots()
    newRoots = newRoots.addEarmarking("Education")
    newRoots = newRoots.addEarmarking("Health")

    val donor = Donor(newRoots.getNewDonorId, "Donor1", "donor1@example.org")
    val donationPart = Donation(donor.id, BigDecimal("100.00"), "Education")
    newRoots = newRoots.donate(donor, donationPart, "Paypal").toOption.get
    val donor2 = Donor(newRoots.getNewDonorId, "Donor2", "donor2@example.org")
    val donationPart2 = Donation(donor2.id, BigDecimal("150.00"), "Health")
    newRoots = newRoots.donate(donor2, donationPart2, "Paypal").toOption.get

    newRoots = newRoots.transfer(BigDecimal("50.00"), "Paypal", "Bank", AppConfigMock()).toOption.get._1

    assertEquals(newRoots.getAccount("Paypal").get.totalBalance, BigDecimal("200.00"))
    assertEquals(newRoots.getAccount("Bank").get.totalBalance, BigDecimal("50.00"))

    // Earmarked balances after partial transfer
    assertEquals(
      newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Education"),
      BigDecimal("50.00")
    )
    assertEquals(
      newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Health"),
      BigDecimal("150.00")
    )

    // The oldest donation (Education) is the one partially transferred
    assertEquals(
      newRoots.getAccount("Bank").get.totalEarmarkedBalance("Education"),
      BigDecimal("50.00")
    )
    assertEquals(
      newRoots.getAccount("Bank").get.totalEarmarkedBalance("Health"),
      BigDecimal("0.00")
    )
  }
