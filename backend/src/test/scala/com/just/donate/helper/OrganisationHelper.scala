package com.just.donate.helper

import cats.effect.IO
import com.just.donate.models.{Account, Donation, Donor, Organisation}
import com.just.donate.store.MemoryStore
import com.just.donate.utils.Money

object OrganisationHelper:

  // Helper method to set up a new Organisation with accounts and flows
  def createNewRoots(): Organisation =
    var newRoots = Organisation("New Roots")

    newRoots = newRoots.addAccount("Paypal")
    newRoots = newRoots.addAccount("Better Place")
    newRoots = newRoots.addAccount("Bank")
    newRoots = newRoots.addAccount("Kenya")

    newRoots

  def addPaypalDonation: IO[Unit] =
    val orgId = organisationId("newRoots")
    for
      newOrg <- MemoryStore
        .load(orgId)
        .map(optOrg =>
          val org = optOrg.get
          val donor = Donor(org.getNewDonorId, "MyDonor", "mydonor@example.org")
          val (donation, donationPart) = Donation(donor.id, Money("100"))
          org.donate(donor, donationPart, donation, "Paypal").toOption.get
        )
      _ <- MemoryStore.save(orgId, newOrg)
    yield ()

  def organisationId(name: String): String = name.hashCode.toString
