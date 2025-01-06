package com.just.donate.models

import com.just.donate.config.Config
import com.just.donate.notify.IEmailService
import com.just.donate.utils.CollectionUtils.updatedReturn

import java.util.UUID
import org.http4s.headers.`Cross-Origin-Resource-Policy`.SameSite

case class Organisation(
  name: String,
  accounts: Seq[Account] = Seq.empty,
  expenses: Seq[Expense] = Seq.empty,
  donors: Map[String, Donor] = Map.empty
):
  def getAccount(name: String): Option[Account] =
    accounts.find(_.name == name)

  def addAccount(name: String): Organisation =
    addAccount(new Account(name))

  def addAccount(account: Account): Organisation =
    if accounts.exists(_.name == account.name) then this
    else copy(accounts = accounts :+ account)

  def removeAccount(name: String): Organisation =
    if accounts.exists(_.name == name) then copy(accounts = accounts.filterNot(_.name == name))
    else this

  def addEarmarking(earmarking: String): Organisation =
    copy(accounts = accounts.map(_.addEarmarking(earmarking)))

  def removeEarmarking(earmarking: String): Organisation =
    copy(accounts = accounts.map(_.removeEarmarking(earmarking)))

  def getExistingDonor(email: String): Option[Donor] = donors.find { case (_, d) => d.email == email }.map(_._2)

  def getNewDonorId: String =
    var newDonorId = UUID.randomUUID().toString

    while donors.contains(newDonorId) do newDonorId = UUID.randomUUID().toString

    newDonorId

  def donate(donor: Donor, donationPart: DonationPart, account: String): Either[DonationError, Organisation] =
    accounts.find(_.name == account) match
      case Some(acc) =>
        val (donated, newAcc) = donationPart.donation.earmarking match
          case Some(earmark) => acc.donate(donationPart, earmark)
          case None          => acc.donate(donationPart)

        if !donated
        then Left(DonationError.INVALID_EARMARKING)
        else
          val newAccounts = accounts.map(a =>
            if a.name == account
            then newAcc
            else a
          )
          val newDonors =
            if donors.contains(donationPart.donation.donorId)
            then donors
            else donors.updated(donor.id, donor)
          Right(copy(accounts = newAccounts, donors = newDonors))
      case None => Left(DonationError.INVALID_ACCOUNT)

  def withdrawal(amount: BigDecimal, account: String, earmarking: Option[String]): Organisation =
    accounts.updatedReturn(a => a.name == account)(a => a.withdrawal(amount, earmarking)) match
      case (newAccounts, Some(donationParts)) =>
        val expense = Expense("description", amount, earmarking, donationParts)
        copy(accounts = newAccounts, expenses = expenses :+ expense)
      case (newAccounts, None) => throw new IllegalArgumentException(s"Account $account does not exist")

  def transfer(
    amount: BigDecimal,
    fromAccount: String,
    toAccount: String,
    config: Config,
    emailService: IEmailService
  ): Either[TransferError, Organisation] =
    (getAccount(fromAccount), getAccount(toAccount)) match
      case (Some(from), Some(to)) => transfer(amount, from, to, config, emailService)
      case (None, _)              => Left(TransferError.INVALID_ACCOUNT)
      case (Some(_), None)        => Left(TransferError.INVALID_ACCOUNT)

  def transfer(
    amount: BigDecimal,
    fromAccount: Account,
    toAccount: Account,
    config: Config,
    emailService: IEmailService
  ): Either[TransferError, Organisation] =
    if fromAccount.totalBalance < amount then return Left(TransferError.INSUFFICIENT_ACCOUNT_FUNDS)

    if amount <= BigDecimal(0) then return Left(TransferError.NON_POSITIVE_AMOUNT)

    if fromAccount.name == toAccount.name then return Left(TransferError.SAME_SOURCE_AND_DESTINATION_ACCOUNT)

    val (remaining, donationPart, earmarked, updatedFrom) = fromAccount.pull(amount)
    val updatedTo = toAccount.push(donationPart, earmarked)

    val updatedOrg = copy(
      accounts = accounts.map(a =>
        if a.name == fromAccount.name then updatedFrom
        else if a.name == toAccount.name then updatedTo
        else a
      )
    )

    val fromQueue = earmarked match
      case None             => updatedFrom.unboundDonations
      case Some(earmarking) => updatedFrom.boundDonations.find { (key, _) => key == earmarking }.get._2
    val fromQueueHasRemainingPart =
      fromQueue.donationQueue.queue.exists(reservable => reservable.value.donation.id == donationPart.donation.id)
    if !fromQueueHasRemainingPart then
      donors.get(donationPart.donation.donorId) match
        case None => return Left(TransferError.INVALID_DONOR)
        case Some(donor) =>
          val trackingId = donor.id
          val trackingLink = f"${config.frontendUrl}/tracking?id=${trackingId}"
          emailService.sendEmail(
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

    if remaining == BigDecimal(0) then Right(updatedOrg)
    else updatedOrg.transfer(remaining, fromAccount, toAccount, config, emailService)

  def totalBalance: BigDecimal =
    accounts.map(_.totalBalance).sum

  def totalEarmarkedBalance(earmarking: String): BigDecimal =
    accounts.map(_.totalEarmarkedBalance(earmarking)).sum

enum DonationError:
  case INVALID_ACCOUNT
  case INVALID_EARMARKING

enum TransferError:
  case INVALID_ACCOUNT
  case INSUFFICIENT_ACCOUNT_FUNDS
  case NON_POSITIVE_AMOUNT
  case SAME_SOURCE_AND_DESTINATION_ACCOUNT
  case INVALID_DONOR
