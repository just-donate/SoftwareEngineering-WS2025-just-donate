package com.just.donate.helper

import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.models.{Account, Donation, Donor, Organisation}

/**
 * A driver object to create and manage organisations.
 */
object OrganisationDriver:

  /**
   * Derives a stable ID from the organisation name.
   */
  private def organisationId(name: String): String = Organisation(name).id

  /**
   * Creates a new organisation with the given name and accounts.
   * @param orgName The name of the organisation.
   * @param accountNames The names of the accounts to create within the organisation.
   * @return The newly created organisation.
   */
  def createOrganisation(
    orgName: String,
    accountNames: String*
  )(repo: Repository[String, Organisation]): IO[Organisation] =
    val baseOrg = Organisation(orgName)
    val finalOrg = accountNames.foldLeft(baseOrg) { (org, accName) =>
      org.addAccount(new Account(accName))
    }

    for
      // Save the newly created organisation in MemoryStore
      _ <- repo.save(finalOrg)
    yield finalOrg

  /**
   * Adds a donation to an existing organisation. If the organisation
   * does not exist, a runtime exception is thrown (you could handle this differently).
   *
   * @param orgName     The organisation name.
   * @param donorName   Name of the donor.
   * @param donorEmail  Email of the donor.
   * @param amount      Donation amount.
   * @param accountName The account (within the organisation) to which the donation is credited.
   * @return A no-result IO, signifying the side effect of saving the updated org.
   */
  def addDonation(
    orgName: String,
    donorName: String,
    donorEmail: String,
    amount: BigDecimal,
    accountName: String
  )(repo: Repository[String, Organisation]): IO[Unit] =
    val orgId = organisationId(orgName)
    for
      maybeOrg <- repo.findById(orgId)
      org = maybeOrg.getOrElse(sys.error(s"Organisation '$orgName' not found"))
      donor = Donor(org.getNewDonorId, donorName, donorEmail)
      // Donation can return donation and donation part
      (donation, donationPart) = Donation(donor.id, amount)
      updatedOrg = org
        .donate(donor, donationPart, donation, accountName)
        .toOption
        .getOrElse(sys.error("Donation failed"))
      _ <- repo.save(updatedOrg)
    yield ()
