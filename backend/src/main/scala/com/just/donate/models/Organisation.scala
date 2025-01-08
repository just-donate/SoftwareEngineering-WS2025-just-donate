package com.just.donate.models

import com.just.donate.config.Config
import com.just.donate.models.Types.DonationGetter
import com.just.donate.models.errors.{ DonationError, TransferError, WithdrawError }
import com.just.donate.notify.EmailMessage

import java.util.UUID

case class Organisation(
  name: String,
  accounts: Map[String, Account] = Map.empty,
  donations: Map[String, Donation] = Map.empty,
  expenses: Seq[Expense] = Seq.empty,
  donors: Map[String, Donor] = Map.empty
):
  def addAccount(name: String): Organisation =
    addAccount(new Account(name))

  def addAccount(account: Account): Organisation =
    if accounts.contains(account.name) then this
    else copy(accounts = accounts.updated(account.name, account))

  def removeAccount(name: String): Organisation =
    if accounts.contains(name) then copy(accounts = accounts.removed(name))
    else this

  def addEarmarking(earmarking: String): Organisation =
    copy(accounts = accounts.map(t => (t._1, t._2.addEarmarking(earmarking))))

  def removeEarmarking(earmarking: String): Organisation =
    copy(accounts = accounts.map(t => (t._1, t._2.removeEarmarking(earmarking))))

  def getExistingDonor(email: String): Option[Donor] = donors.find { case (_, d) => d.email == email }.map(_._2)

  def getNewDonorId: String =
    var newDonorId = UUID.randomUUID().toString

    while donors.contains(newDonorId) do newDonorId = UUID.randomUUID().toString

    newDonorId

  given DonationGetter = getDonation

  def getDonation: DonationGetter = donations.get

  def donate(
    donor: Donor,
    donationPart: DonationPart,
    donation: Donation,
    account: String
  ): Either[DonationError, Organisation] =
    copy(donations = donations.updated(donation.id, donation)).donate(donor, donationPart, account)

  private def donate(
    donor: Donor,
    donationPart: DonationPart,
    account: String
  ): Either[DonationError, Organisation] =
    accounts.get(account) match
      case None => Left(DonationError.INVALID_ACCOUNT)
      case Some(acc) =>
        val (donated, newAcc) = donationPart.donation.get.earmarking match
          case Some(earmark) => acc.donate(donationPart, earmark)
          case None          => acc.donate(donationPart)

        if !donated
        then Left(DonationError.INVALID_EARMARKING)
        else
          val newAccounts = accounts.updated(account, newAcc)
          val newDonors =
            if donors.contains(donationPart.donation.get.donorId)
            then donors
            else donors.updated(donor.id, donor)
          Right(copy(accounts = newAccounts, donors = newDonors))

  def withdrawal(
    amount: BigDecimal,
    accountName: String,
    description: String,
    earmarking: Option[String],
    config: Config
  ): Either[WithdrawError, (Organisation, Seq[EmailMessage])] =
    getAccount(accountName) match
      case None          => Left(WithdrawError.INVALID_ACCOUNT)
      case Some(account) => withdrawal(amount, account, description, earmarking, config)

  def getAccount(name: String): Option[Account] =
    accounts.get(name)

  def withdrawal(
    amount: BigDecimal,
    account: Account,
    description: String,
    earmarking: Option[String],
    config: Config
  ): Either[WithdrawError, (Organisation, Seq[EmailMessage])] =
    val (donationParts, updatedAccount) = account.withdrawal(amount, earmarking)

    val newAccounts = accounts.updated(account.name, updatedAccount)
    val expense = Expense(description, amount, earmarking, donationParts)
    val updatedOrg = copy(accounts = newAccounts, expenses = expenses.appended(expense))

    updatedOrg.getNotificationsForUtilizedDonations(donationParts, config).map(messages => (updatedOrg, messages))

  private def getNotificationsForUtilizedDonations(
    usedDonationParts: Seq[DonationPart],
    config: Config
  ): Either[WithdrawError, Seq[EmailMessage]] =
    var remainingSeq = usedDonationParts
    var emailMessages: Seq[EmailMessage] = Seq()

    while remainingSeq.nonEmpty do
      val donationPart = remainingSeq.head
      remainingSeq = remainingSeq.tail

      val donationIsFullyUsed = donationPart.donation.get.amountRemaining == BigDecimal(0)
      if donationIsFullyUsed then
        val donor = donors.get(donationPart.donation.get.donorId) match
          case None        => return Left(WithdrawError.INVALID_DONOR)
          case Some(donor) => donor
        val trackingId = donor.id
        val trackingLink = f"${config.frontendUrl}/tracking?id=${trackingId}"

        emailMessages.appended(
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

  def transfer(
    amount: BigDecimal,
    fromAccount: String,
    toAccount: String,
    config: Config
  ): Either[TransferError, (Organisation, Seq[EmailMessage])] =
    (getAccount(fromAccount), getAccount(toAccount)) match
      case (Some(from), Some(to)) => transfer(amount, from, to, config)
      case (None, _)              => Left(TransferError.INVALID_ACCOUNT)
      case (Some(_), None)        => Left(TransferError.INVALID_ACCOUNT)

  def transfer(
    amount: BigDecimal,
    fromAccount: Account,
    toAccount: Account,
    config: Config
  ): Either[TransferError, (Organisation, Seq[EmailMessage])] =
    if fromAccount.totalBalance < amount then return Left(TransferError.INSUFFICIENT_ACCOUNT_FUNDS)

    if amount <= BigDecimal(0) then return Left(TransferError.NON_POSITIVE_AMOUNT)

    if fromAccount.name == toAccount.name then return Left(TransferError.SAME_SOURCE_AND_DESTINATION_ACCOUNT)

    val (remaining, donationPart, earmarked, updatedFrom) = fromAccount.pull(amount)
    val updatedTo = toAccount.push(donationPart, earmarked)

    val updatedOrg = copy(
      accounts = accounts.updated(fromAccount.name, updatedFrom).updated(toAccount.name, updatedTo)
    )

    val fromQueue = earmarked match
      case None             => updatedFrom.unboundDonations
      case Some(earmarking) => updatedFrom.boundDonations.find { (key, _) => key == earmarking }.get._2
    val fromQueueHasRemainingPart =
      fromQueue.donationQueue.queue.exists(reservable =>
        reservable.value.donation.get.id == donationPart.donation.get.id
      )

    val emailMessage: Option[EmailMessage] =
      if !fromQueueHasRemainingPart then
        donors.get(donationPart.donation.get.donorId) match
          case None => return Left(TransferError.INVALID_DONOR)
          case Some(donor) =>
            val trackingId = donor.id
            val trackingLink = f"${config.frontendUrl}/tracking?id=${trackingId}"
            Some(
              EmailMessage(
                donor.email,
                f"""Your recent donation to ${name} has been fully transferred away from the account ${fromAccount.name}.
                   |To see more details about the status of your donation, visit the following link
                   |${trackingLink}
                   |or enter your tracking id
                   |${trackingId}
                   |on our tracking page
                   |${config.frontendUrl}""".stripMargin,
                "Just Donate: News about your donation"
              )
            )
      else None

    if remaining == BigDecimal(0) then Right(updatedOrg, emailMessage.toSeq)
    else
      updatedOrg.transfer(remaining, fromAccount, toAccount, config).map {
        case (org, emailMessages) => (org, emailMessages.prependedAll(emailMessage))
      }

  def totalBalance: BigDecimal =
    accounts.map(_._2.totalBalance).sum

  def totalEarmarkedBalance(earmarking: String): BigDecimal =
    accounts.map(_._2.totalEarmarkedBalance(earmarking)).sum
