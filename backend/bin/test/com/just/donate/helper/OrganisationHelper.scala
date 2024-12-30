package com.just.donate.helper

import com.just.donate.models.{Account, Organisation}

object OrganisationHelper:

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
