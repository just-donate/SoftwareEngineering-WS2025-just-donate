package com.just.donate.models

import com.just.donate.config.Config
import com.just.donate.models.Types.DonationGetter
import com.just.donate.models.EarmarkingImage
import com.just.donate.models.errors.{DonationError, TransferError, WithdrawError}
import com.just.donate.notify.EmailMessage
import com.just.donate.utils.Money

import java.time.LocalDateTime
import java.util.UUID
import scala.math.Ordered.orderingToOrdered

case class Organisation(
  name: String,
  accounts: Map[String, Account] = Map.empty,
  donations: Map[String, Donation] = Map.empty,
  expenses: Seq[Expense] = Seq.empty,
  donors: Map[String, Donor] = Map.empty,
  earmarkings: Seq[Earmarking] = Seq.empty,
  theme: Option[ThemeConfig] = None,
  earmarkingImages: Map[String, Seq[EarmarkingImage]] = Map.empty
):

  def id: String = math.abs(name.hashCode).toString

  def getEarmarkings: Set[Earmarking] = earmarkings.toSet

  def getEarmarking(name: String): Option[Earmarking] = earmarkings.find(e => e.name == name || e.id == name)

  /**
   * Add a new earmarking image to the organisation.
   * @param earmarking the name of the earmarking.
   * @param image the image to add.
   * @return a new organisation with the earmarking image added.
   */
  def addEarmarkingImage(earmarking: String, image: EarmarkingImage): Organisation =
    copy(earmarkingImages = earmarkingImages.updated(earmarking, earmarkingImages.getOrElse(earmarking, Seq.empty).appended(image)))

  def getEarmarkingImages(earmarking: String): Option[Seq[EarmarkingImage]] = earmarkingImages.get(earmarking)

  def setTheme(theme: ThemeConfig): Organisation = copy(theme = Some(theme))

  /**
   * Add a new account to the organisation.
   * @param name the name of the account.
   * @return a new organisation with the account added.
   */
  def addAccount(name: String, initialBalance: Money = Money.ZERO): Organisation =
    val initialAccount = getEarmarkings.foldLeft(new Account(name))(_.addEarmarking(_))
    // TODO add initial balance
    if accounts.contains(initialAccount.name) then this
    else copy(accounts = accounts.updated(initialAccount.name, initialAccount))

  /**
   * Remove an account from the organisation.
   * @param name the name of the account.
   * @return a new organisation with the account removed.
   */
  def removeAccount(name: String): Organisation =
    if accounts.contains(name) then copy(accounts = accounts.removed(name))
    else this

  /**
   * Add a new earmarking to all accounts in the organisation.
   * @param earmarking the name of the earmarking.
   * @return a new organisation with the earmarking added to all accounts.
   */
  def addEarmarking(earmarking: Earmarking): Organisation =
    copy(
      accounts = accounts.map(t => (t._1, t._2.addEarmarking(earmarking))),
      earmarkings = earmarkings.appended(earmarking)
    )

  /**
   * Remove an earmarking from all accounts in the organisation.
   * @param earmarking the name of the earmarking.
   * @return a new organisation with the earmarking removed from all accounts.
   */
  def removeEarmarking(earmarking: String): Organisation =
    getEarmarking(earmarking) match
      case Some(earmark) => copy(accounts = accounts.map(t => (t._1, t._2.removeEarmarking(earmark))))
      case None          => this

  def getDonations: Seq[Donation] = donations.values.toSeq

  /**
   * Loads an existing donor from the organisation.
   * @param email the email of the donor.
   * @return an option of the donor, depending on whether the donor exists.
   */
  def getExistingDonor(email: String): Option[Donor] = donors.find(_._2.email == email).map(_._2)

  /**
   * Get a new donor id which is not already in use.
   * @return a new donor id.
   */
  def getNewDonorId: String =
    var newDonorId = UUID.randomUUID().toString
    while donors.contains(newDonorId) do newDonorId = UUID.randomUUID().toString
    newDonorId

  /**
   * Get a donation by id of the currently active organisation. This function can be summoned
   * to get the donation, when the organisation is in scope, for example inside the account.
   *
   * Example:
   * `def someFunction(...)(using donationGetter: DonationGetter): Unit = { ... }`
   * @return A function which gets a donation by id from the organisation.
   */
  given DonationGetter = getDonation

  private def getDonation: DonationGetter = donations.get

  /**
   * Donate to the organisation. This function is the entry point for donating to the organisation.
   * @param donor the donor who is donating.
   * @param donationPart the donation part which is being donated.
   * @param donation the donation which is being donated.
   * @param account the account to which the donation is being made.
   * @return either an error or the updated organisation.
   */
  def donate(
    donor: Donor,
    donationPart: DonationPart,
    donation: Donation,
    account: String
  ): Either[DonationError, Organisation] =
    // We first insert the donation into the donations, then we donate to the account, this is
    // done to ensure that the donation is available in the organisation during the process.
    donation.addStatusUpdate(
      StatusUpdate(
        LocalDateTime.now(),
        StatusUpdate.Status.RECEIVED,
        "Donation has been made and has been added to the account: " + account
      )
    )
    copy(donations = donations.updated(donation.id, donation)).donate(donor, donationPart, account)

  private def donate(
    donor: Donor,
    donationPart: DonationPart,
    account: String
  ): Either[DonationError, Organisation] =
    getAccount(account) match
      case None => Left(DonationError.INVALID_ACCOUNT)
      case Some(acc) =>
        val newAccount = donationPart.earmarking match
          case Some(earmark) => acc.donate(donationPart, earmark)
          case None          => acc.donate(donationPart)

        newAccount match
          case Left(e) => Left(e)
          case Right(newAcc) =>
            val newAccounts = accounts.updated(account, newAcc)
            val newDonors =
              if donors.contains(donationPart.donation.get.donorId) then donors
              else donors.updated(donor.id, donor)
            Right(copy(accounts = newAccounts, donors = newDonors))

  /**
   * Get an account by name.
   * @param name the name of the account.
   * @return an option of the account, depending on whether the account exists.
   */
  def getAccount(name: String): Option[Account] = accounts.get(name)

  /**
   * Withdraw from the organisation. This function is the entry point for withdrawing from the organisation.
   * @param amount the amount to withdraw.
   * @param accountName the name of the account to withdraw from.
   * @param description the description of the withdrawal.
   * @param earmarking the earmarking of the withdrawal.
   * @param config the configuration of the organisation.
   * @return either an error or the updated organisation and email messages.
   */
  def withdrawal(
    amount: Money,
    accountName: String,
    description: String,
    earmarking: Option[Earmarking],
    config: Config
  ): Either[WithdrawError, (Organisation, Seq[EmailMessage])] =
    getAccount(accountName) match
      case None          => Left(WithdrawError.INVALID_ACCOUNT)
      case Some(account) => withdrawal(amount, account, description, earmarking, config)

  def withdrawal(
    amount: Money,
    account: Account,
    description: String,
    earmarking: Option[Earmarking],
    config: Config
  ): Either[WithdrawError, (Organisation, Seq[EmailMessage])] =
    account.withdrawal(amount, earmarking) match
      case Left(value) => Left(value)
      case Right((donationParts, updatedAccount)) =>
        val newAccounts = accounts.updated(account.name, updatedAccount)
        val expense = Expense(description, amount, earmarking, donationParts)
        val newDonations = getDonationsAfterWithdrawal(donationParts) match
          case Left(error)  => return Left(error)
          case Right(value) => value

        val updatedOrg = copy(accounts = newAccounts, donations = newDonations, expenses = expenses.appended(expense))

        updatedOrg.getNotificationsForUtilizedDonations(donationParts, config).map(messages => (updatedOrg, messages))

  /**
   * Transfer between accounts in the organisation. This function is the entry point for transferring between accounts.
   * @param amount the amount to transfer.
   * @param fromAccount the name of the account to transfer from.
   * @param toAccount the name of the account to transfer to.
   * @param config the configuration of the organisation.
   * @return either an error or the updated organisation and email messages.
   */
  def transfer(
    amount: Money,
    fromAccount: String,
    toAccount: String,
    config: Config
  ): Either[TransferError, (Organisation, Seq[EmailMessage])] =
    (getAccount(fromAccount), getAccount(toAccount)) match
      case (Some(from), Some(to)) => transfer(amount, from, to, config)
      case _                      => Left(TransferError.INVALID_ACCOUNT)

  /**
   * Transfer between accounts in the organisation. This function is the entry point for transferring between accounts.
   * @param amount the amount to transfer.
   * @param fromAccount the account to transfer from.
   * @param toAccount the account to transfer to.
   * @param config the configuration of the organisation.
   * @return either an error or the updated organisation and email messages.
   */
  def transfer(
    amount: Money,
    fromAccount: Account,
    toAccount: Account,
    config: Config
  ): Either[TransferError, (Organisation, Seq[EmailMessage])] =
    if fromAccount.totalBalance < amount then return Left(TransferError.INSUFFICIENT_ACCOUNT_FUNDS)

    if amount <= Money.ZERO then return Left(TransferError.NON_POSITIVE_AMOUNT)

    if fromAccount.name == toAccount.name then return Left(TransferError.SAME_SOURCE_AND_DESTINATION_ACCOUNT)

    val (parts, updatedFrom) = fromAccount.pull(amount)
    val updatedTo = parts.foldLeft(toAccount): (account, donationPart) =>
      donationPart match
        case (Some(earmarking), donationPart) => account.donate(donationPart, earmarking).toOption.get
        case (None, donationPart)             => account.donate(donationPart).toOption.get

    val updatedOrg = copy(
      accounts = accounts.updated(fromAccount.name, updatedFrom).updated(toAccount.name, updatedTo)
    )

    Right((updatedOrg, Seq.empty[EmailMessage]))

  def totalBalance: Money =
    accounts.map(_._2.totalBalance).sum

  def totalEarmarkedBalance(earmarking: Earmarking): Money =
    accounts.map(_._2.totalEarmarkedBalance(earmarking)).sum

  def getDonations(donorId: String): Seq[Donation] =
    donations.values.filter(_.donorId == donorId).toSeq

  private def getDonationsAfterWithdrawal(
    donationParts: Seq[DonationPart]
  ): Either[WithdrawError, Map[String, Donation]] =
    donationParts.headOption match
      case None => Right(donations)
      case Some(donationPart) =>
        getDonationsAfterWithdrawal(
          donationParts.tail
        ).map(_.updatedWith(donationPart.donationId)(optDonation =>
          val donation = optDonation.get
          Some(donation.copy(amountRemaining = donation.amountRemaining - donationPart.amount))
        ))

  private def getNotificationsForUtilizedDonations(
    usedDonationParts: Seq[DonationPart],
    config: Config
  ): Either[WithdrawError, Seq[EmailMessage]] =
    var remainingSeq = usedDonationParts
    var emailMessages: Seq[EmailMessage] = Seq()

    while remainingSeq.nonEmpty do
      val donationPart = remainingSeq.head
      remainingSeq = remainingSeq.tail

      val donationIsFullyUsed = donationPart.donation.get.amountRemaining == Money.ZERO
      if donationIsFullyUsed then
        val donor = donors.get(donationPart.donation.get.donorId) match
          case None        => return Left(WithdrawError.INVALID_DONOR)
          case Some(donor) => donor
        val trackingId = donor.id
        val trackingLink = f"${config.frontendUrl}/tracking?id=$trackingId}"

        emailMessages = emailMessages.appended(
          EmailMessage(
            donor.email,
            f"""Your recent donation to ${name} has been fully utilized.
               |To see more details about the status of your donation, visit the following link
               |${trackingLink}
               |or enter your tracking id
               |${trackingId}
               |on our tracking page
               |${config.frontendUrl}""".stripMargin,
            "Just Donate: Your donation has been utilized"
          )
        )

    Right(emailMessages)
