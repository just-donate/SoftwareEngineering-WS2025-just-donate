package com.just.donate.helper

import com.just.donate.models.{Account, Organisation}
import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.models.Donor
import com.just.donate.models.Donation

object OrganisationHelper:
  
  val NEW_ROOTS = "New Roots"

  // Helper method to set up a new Organisation with accounts and flows
  def createNewRoots(): Organisation =
    var newRoots = Organisation(NEW_ROOTS)

    val paypal = new Account("Paypal")
    val betterPlace = new Account("Better Place")
    val bank = new Account("Bank")
    val kenya = new Account("Kenya")

    newRoots = newRoots.addAccount(paypal)
    newRoots = newRoots.addAccount(betterPlace)
    newRoots = newRoots.addAccount(bank)
    newRoots = newRoots.addAccount(kenya)

    newRoots

  def addPaypalDonation(repo: Repository[String, Organisation]): IO[Unit] =
    val orgId = organisationId(NEW_ROOTS)
    for
      newOrg <- repo
        .findById(orgId)
        .map(optOrg =>
          val org = optOrg.get
          val donor = Donor(org.getNewDonorId, "MyDonor", "mydonor@example.org")
          val (donation, donationPart) = Donation(donor.id, BigDecimal(100))
          org.donate(donor, donationPart, donation, "Paypal").toOption.get
        )
      _ <- repo.save(newOrg)
    yield ()

  def organisationId(name: String): String = Organisation(name).id
