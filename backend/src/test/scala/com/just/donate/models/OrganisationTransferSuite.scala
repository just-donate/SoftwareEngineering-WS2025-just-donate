package com.just.donate.models

import com.just.donate.helper.OrganisationHelper.createNewRoots
import munit.FunSuite

class OrganisationTransferSuite extends FunSuite {

  test("transfer money between accounts") {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")
    newRoots = newRoots.transfer(BigDecimal("50.00"), "Paypal", "Bank")

    assertEquals(newRoots.getAccount("Paypal").get.totalBalance, BigDecimal("50.00"))
    assertEquals(newRoots.getAccount("Bank").get.totalBalance, BigDecimal("50.00"))
  }

  test("not transfer money if the source account does not have enough balance") {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    intercept[IllegalStateException] {
      newRoots.transfer(BigDecimal("150.00"), "Paypal", "Bank")
    }
  }

  test("not transfer money if the source account does not exist") {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    intercept[IllegalArgumentException] {
      newRoots.transfer(BigDecimal("50.00"), "NoExists", "Bank")
    }
  }

  test("not transfer money if the destination account does not exist") {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    intercept[IllegalArgumentException] {
      newRoots.transfer(BigDecimal("50.00"), "Paypal", "NoExists")
    }
  }

  test("not transfer money if the amount is negative") {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    intercept[IllegalArgumentException] {
      newRoots.transfer(BigDecimal("-50.00"), "Paypal", "Bank")
    }
  }

  test("not transfer money if the amount is zero") {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    intercept[IllegalArgumentException] {
      newRoots.transfer(BigDecimal("0.00"), "Paypal", "Bank")
    }
  }

  test("not transfer money if the source account is the same as the destination account") {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    intercept[IllegalArgumentException] {
      newRoots.transfer(BigDecimal("50.00"), "Paypal", "Paypal")
    }
  }

  test("transfer earmarked money retains its earmarking after transfer") {
    var newRoots = createNewRoots()

    newRoots = newRoots.addEarmarking("Education")
    newRoots = newRoots.donate("Donor1", BigDecimal("200.00"), Some("Education"), "Paypal")
    newRoots = newRoots.transfer(BigDecimal("100.00"), "Paypal", "Bank")

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

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), Some("Education"), "Paypal")
    newRoots = newRoots.donate("Donor2", BigDecimal("150.00"), Some("Health"), "Paypal")

    newRoots = newRoots.transfer(BigDecimal("50.00"), "Paypal", "Bank")

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
}



