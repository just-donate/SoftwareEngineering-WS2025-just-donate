package com.just.donate.helper

import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.models.{Account, Donation, Donor, Organisation}
import com.just.donate.utils.Money

object OrganisationHelper:

  val NEW_ROOTS = "New Roots"

  // Helper method to set up a new Organisation with accounts and flows
  def createNewRoots(): Organisation =
    var newRoots = Organisation(NEW_ROOTS)

    newRoots = newRoots.addAccount("Paypal")
    newRoots = newRoots.addAccount("Better Place")
    newRoots = newRoots.addAccount("Bank")
    newRoots = newRoots.addAccount("Kenya")

    newRoots

  def addPaypalDonation(repo: Repository[String, Organisation]): IO[Unit] =
    val orgId = organisationId(NEW_ROOTS)
    for
      newOrg <- repo
        .findById(orgId)
        .map(optOrg =>
          val org = optOrg.get
          val donor = Donor(org.getNewDonorId, "MyDonor", "mydonor@example.org")
          val (donation, donationPart) = Donation(donor.id, Money("100"))
          org.donate(donor, donationPart, donation, "Paypal").toOption.get
        )
      _ <- repo.save(newOrg)
    yield ()

  def organisationId(name: String): String = Organisation(name).id
