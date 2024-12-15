package com.just.donate.models

import com.just.donate.helper.OrganisationHelper.createNewRoots
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OrganisationTransferTest extends AnyFlatSpec with Matchers:

  "An OrganisationTransfer" should "transfer money between accounts" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")
    newRoots = newRoots.transfer(BigDecimal("50.00"), "Paypal", "Bank")

    newRoots.getAccount("Paypal").get.totalBalance shouldEqual BigDecimal("50.00")
    newRoots.getAccount("Bank").get.totalBalance shouldEqual BigDecimal("50.00")
  }

  it should "not transfer money if the source account does not have enough balance" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    an[IllegalStateException] should be thrownBy {
      newRoots.transfer(BigDecimal("150.00"), "Paypal", "Bank")
    }
  }

  it should "not transfer money if the source account does not exist" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    an[IllegalStateException] should be thrownBy {
      newRoots.transfer(BigDecimal("50.00"), "Paypal", "Bank")
    }
  }

  it should "not transfer money if the destination account does not exist" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    an[IllegalStateException] should be thrownBy {
      newRoots.transfer(BigDecimal("50.00"), "Paypal", "Bank")
    }
  }

  it should "not transfer money if the amount is negative" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    an[IllegalArgumentException] should be thrownBy {
      newRoots.transfer(BigDecimal("-50.00"), "Paypal", "Bank")
    }
  }

  it should "not transfer money if the amount is zero" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    an[IllegalArgumentException] should be thrownBy {
      newRoots.transfer(BigDecimal("0.00"), "Paypal", "Bank")
    }
  }

  it should "not transfer money if the source account is the same as the destination account" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), None, "Paypal")

    an[IllegalArgumentException] should be thrownBy {
      newRoots.transfer(BigDecimal("50.00"), "Paypal", "Paypal")
    }
  }

  it should "transfer earmarked money should keep its earmarking after transfer" in {
    var newRoots = createNewRoots()

    newRoots = newRoots.addEarmarking("Education")
    newRoots = newRoots.donate("Donor1", BigDecimal("200.00"), Some("Education"), "Paypal")
    newRoots = newRoots.transfer(BigDecimal("100.00"), "Paypal", "Bank")

    newRoots.getAccount("Paypal").get.totalBalance shouldEqual BigDecimal("100.00")
    newRoots.getAccount("Bank").get.totalBalance shouldEqual BigDecimal("100.00")

    newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Education") shouldEqual BigDecimal("100.00")
    newRoots.getAccount("Bank").get.totalEarmarkedBalance("Education") shouldEqual BigDecimal("100.00")
  }

  it should "transfer always the oldest donation if multiple are available" in {
    var newRoots = createNewRoots()
    newRoots = newRoots.addEarmarking("Education")
    newRoots = newRoots.addEarmarking("Health")

    newRoots = newRoots.donate("Donor1", BigDecimal("100.00"), Some("Education"), "Paypal")
    newRoots = newRoots.donate("Donor2", BigDecimal("150.00"), Some("Health"), "Paypal")

    newRoots = newRoots.transfer(BigDecimal("50.00"), "Paypal", "Bank")

    newRoots.getAccount("Paypal").get.totalBalance shouldEqual BigDecimal("200.00")
    newRoots.getAccount("Bank").get.totalBalance shouldEqual BigDecimal("50.00")

    newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Education") shouldEqual BigDecimal("50.00")
    newRoots.getAccount("Paypal").get.totalEarmarkedBalance("Health") shouldEqual BigDecimal("150.00")

    newRoots.getAccount("Bank").get.totalEarmarkedBalance("Education") shouldEqual BigDecimal("0.00")
    newRoots.getAccount("Bank").get.totalEarmarkedBalance("Health") shouldEqual BigDecimal("50.00")
  }


