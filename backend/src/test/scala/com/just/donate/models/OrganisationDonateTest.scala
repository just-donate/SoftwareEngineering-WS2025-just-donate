package com.just.donate.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OrganisationDonateTest extends AnyFlatSpec with Matchers:

  // Helper method to set up a new Organisation with accounts and flows
  def createNewRoots(): Organisation =
    var newRoots = Organisation("New Roots")

    val paypal = new Account("Paypal")
    val betterPlace = new Account("Better Place")
    val bank = new Account("Bank")
    val kenya = new Account("Kenya")

    newRoots = newRoots.addAccount(paypal)
    newRoots = newRoots.addAccount(betterPlace)
    newRoots = newRoots.addAccount(bank)
    newRoots = newRoots.addAccount(kenya)

    newRoots

  "An Organisation" should "have a total balance of zero without any donations or expenses" in {
    val newRoots = createNewRoots()
    newRoots.totalBalance shouldEqual BigDecimal(0)
  }

  it should "reflect unbound donations in the total balance" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail("Paypal account not found"))

    paypal.totalBalance shouldEqual BigDecimal("100.00")
    newRoots.totalBalance shouldEqual BigDecimal("100.00")
  }

  it should "reflect bound donations in the total balance and earmarked balances" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.addEarmarking("Education")
    newRoots = newRoots.donate("Donor1", BigDecimal("200.00"), Some("Education"), "Paypal")

    val paypalOption = newRoots.getAccount("Paypal")
    val paypal = paypalOption.getOrElse(fail("Paypal account not found"))

    paypal.totalBalance shouldEqual BigDecimal("200.00")
    newRoots.totalBalance shouldEqual BigDecimal("200.00")

    paypal.totalEarmarkedBalance("Education") shouldEqual BigDecimal("200.00")
    newRoots.totalEarmarkedBalance("Education") shouldEqual BigDecimal("200.00")

    // TODO: Fix this test as it was commented out due to an error
    // an [IllegalStateException] should be thrownBy {
    //   newRoots.totalEarmarkedBalance("Health")
    // }
  }

  it should "aggregate donations from multiple accounts correctly in the total balance" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")
    newRoots = newRoots.donate("Donor2", BigDecimal("150.00"), None, "Bank")

    val paypal = newRoots.getAccount("Paypal")
    val bank = newRoots.getAccount("Bank")

    paypal.get.totalBalance shouldEqual BigDecimal("100.00")
    bank.get.totalBalance shouldEqual BigDecimal("150.00")
    newRoots.totalBalance shouldEqual BigDecimal("250.00")
  }

  it should "correctly update the total balance when donations are made to accounts with incoming flows" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")
    newRoots = newRoots.donate("Donor2", BigDecimal("150.00"), None, "Bank")

    val paypal = newRoots.getAccount("Paypal")
    val bank = newRoots.getAccount("Bank")

    paypal.get.totalBalance shouldEqual BigDecimal("100.00")
    bank.get.totalBalance shouldEqual BigDecimal("150.00")
    newRoots.totalBalance shouldEqual BigDecimal("250.00")

    // Adding donation to an account with incoming flows
    newRoots = newRoots.donate("Donor3", BigDecimal("200.00"), None, "Kenya")

    val kenya = newRoots.getAccount("Kenya")
    kenya.get.totalBalance shouldEqual BigDecimal("200.00")
    newRoots.totalBalance shouldEqual BigDecimal("450.00")
  }
