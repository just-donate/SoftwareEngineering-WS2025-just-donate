package com.just.donate.models

import com.just.donate.helper.OrganisationHelper.createNewRoots
import munit.FunSuite

class OrganisationDonateSuite extends FunSuite:

  test("have a total balance of zero without any donations or expenses") {
    val newRoots = createNewRoots()
    assertEquals(newRoots.totalBalance, BigDecimal(0))
  }

  test("reflect unbound donations in the total balance") {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail("Paypal account not found"))

    assertEquals(paypal.totalBalance, BigDecimal("100.00"))
    assertEquals(newRoots.totalBalance, BigDecimal("100.00"))
  }

  test("reflect bound donations in the total balance and earmarked balances") {
    var newRoots = createNewRoots()

    newRoots = newRoots.addEarmarking("Education")
    newRoots = newRoots.donate("Donor1", BigDecimal("200.00"), Some("Education"), "Paypal")

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

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")
    newRoots = newRoots.donate("Donor2", BigDecimal("150.00"), None, "Bank")

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

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")
    newRoots = newRoots.donate("Donor2", BigDecimal("150.00"), None, "Bank")

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail("Paypal account not found"))

    val bankOption = newRoots.getAccount("Bank")
    val bank = bankOption.getOrElse(fail("Bank account not found"))

    assertEquals(paypal.totalBalance, BigDecimal("100.00"))
    assertEquals(bank.totalBalance, BigDecimal("150.00"))
    assertEquals(newRoots.totalBalance, BigDecimal("250.00"))

    // Adding donation to an account with incoming flows
    newRoots = newRoots.donate("Donor3", BigDecimal("200.00"), None, "Kenya")

    val kenyaOption = newRoots.getAccount("Kenya")
    val kenya = kenyaOption.getOrElse(fail("Kenya account not found"))

    assertEquals(kenya.totalBalance, BigDecimal("200.00"))
    assertEquals(newRoots.totalBalance, BigDecimal("450.00"))
  }
