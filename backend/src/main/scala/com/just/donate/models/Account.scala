package com.just.donate.models

import com.just.donate.models.Types.DonationGetter
import com.just.donate.models.errors.{ DonationError, WithdrawError }
import com.just.donate.utils.CollectionUtils.{ updated, updatedReturn }
import com.just.donate.utils.Money
import com.just.donate.utils.structs.ReservableQueue

import scala.math.Ordered.orderingToOrdered

case class Account private (
  name: String,
  boundDonations: Seq[(Earmarking, DonationQueue)],
  unboundDonations: DonationQueue
):

  def this(name: String) = this(name, Seq.empty, DonationQueue(name, ReservableQueue(name)))

  /**
   * Removes earmarking from the account.
   * @param earmarking earmarking to remove.
   * @return account without earmarking.
   */
  def removeEarmarking(earmarking: Earmarking): Account =
    getBoundQueue(earmarking) match
      case None => throw new IllegalArgumentException("Earmarking not found")
      case Some(earmarkingQueue) =>
        earmarkingQueue.totalBalance match
          case Money.ZERO => copy(boundDonations = boundDonations.filterNot(_._1 == earmarking))
          case _          => throw new IllegalArgumentException("Earmarking has budget")

  private def getBoundQueue(earmarking: Earmarking): Option[DonationQueue] =
    boundDonations.filter(_._1 == earmarking).map(_._2).headOption

  def addEarmarking(earmarking: Earmarking): Account =
    copy(boundDonations = boundDonations :+ ((earmarking, DonationQueue(name, ReservableQueue(name)))))

  def totalEarmarkedBalance(earmarking: Earmarking): Money =
    getBoundQueue(earmarking).map(_.totalBalance).getOrElse(Money.ZERO)

  def withdrawal(amount: Money, earmarking: Option[Earmarking]): Either[WithdrawError, (Seq[DonationPart], Account)] =
    if totalBalance < amount then
      println("acc - insufficient funds")
      Left(WithdrawError.INSUFFICIENT_ACCOUNT_FUNDS)
    else
      earmarking match
        case Some(em) => withdrawalBound(amount, em)
        case None     => withdrawalUnbound(amount)

  private def withdrawalUnbound(amount: Money): Either[WithdrawError, (Seq[DonationPart], Account)] =
    // 1. Expense is unbound, withdraw from unbound donations
    //    - if unbound are not enough, go into minus as long it is covered by bound donations
    //      (we don't need to subtract form bound, as they are reserved and the organization
    //       must cover the expense from unbound later on)
    // 2. Expense is unbound, but not enough unbound donations, return false

    // TODO: For now a simple implementation, where we just pull the amount from the unbound donations
    val (donationParts, remaining, updatedFrom) = this.unboundDonations.pull(amount)

    if remaining.isDefined && remaining.get > Money.ZERO then
      println("acc unbound - insufficient funds")
      Left(WithdrawError.INSUFFICIENT_ACCOUNT_FUNDS)
    else Right(donationParts, copy(unboundDonations = updatedFrom))

  private def withdrawalBound(
    amount: Money,
    earmarking: Earmarking
  ): Either[WithdrawError, (Seq[DonationPart], Account)] =
    // 1. Expense is bound, withdraw from bound donations
    //    - if bound are not enough, check if up the queue are more and reserve them, go into minus
    //      as long it is covered by unbound donations
    // 2. Expense is bound, but not enough bound donations up queue, withdraw from unbound donations.
    //    - if unbound are not enough, do not go into minus, as an account must be covered, return false

    // TODO: For now a simple implementation, where we just pull the amount from the bound donations
    val (updatedFrom, returned) =
      boundDonations.updatedReturn(b => b._1 == earmarking)((earmarking, queue) =>
        val (donationParts, remaining, updatedQueue) = queue.pull(amount)
        ((remaining, donationParts), (earmarking, updatedQueue))
      )

    returned match
      case None => Left(WithdrawError.INVALID_EARMARKING)
      case Some((remaining, donationParts)) =>
        remaining match
          case Some(d) =>
            println("acc bound - insufficient funds")
            Left(WithdrawError.INSUFFICIENT_ACCOUNT_FUNDS)
          case None => Right((donationParts, copy(boundDonations = updatedFrom)))

  def totalBalance: Money = totalBalanceUnbound + totalBalanceBound

  private def totalBalanceUnbound: Money = unboundDonations.totalBalance

  private def totalBalanceBound: Money = boundDonations.map(_._2.totalBalance).sum

  def donate(donation: DonationPart, earmarking: Option[Earmarking] = None): Either[DonationError, Account] =
    earmarking match
      case Some(earmark) => donate(donation, earmark)
      case None          => Right(copy(unboundDonations = unboundDonations.add(donation)))

  def donate(donation: DonationPart, earmarking: Earmarking): Either[DonationError, Account] =
    def donateRec(queues: Seq[(Earmarking, DonationQueue)]): (Boolean, Seq[(Earmarking, DonationQueue)]) = queues match
      case Nil => (false, Nil)
      case (earmarkingQueue, queue) :: tail =>
        if earmarking == earmarkingQueue then (true, (earmarkingQueue, queue.add(donation)) +: tail)
        else
          val (donated, newQueues) = donateRec(tail)
          (donated, (earmarkingQueue, queue) +: newQueues)

    val (donated, newBoundDonations) = donateRec(boundDonations)
    if donated then Right(copy(boundDonations = newBoundDonations))
    else Right(this)

  private[models] def pull(amount: Money)(using
    donationGetter: DonationGetter
  ): (Seq[(Option[Earmarking], DonationPart)], Account) =
    // TODO: change to actual error handling
    if totalBalance < amount then throw new IllegalArgumentException(s"Account $name has insufficient funds")

    val (earmarking, queue) = findQueueWithOldestDonation
    val (donationPart, remaining, updatedQueue) = queue.pull(amount, Some(1))

    val updatedAccount = earmarking match
      case None    => copy(unboundDonations = updatedQueue)
      case Some(e) => copy(boundDonations = boundDonations.updated(b => b._1 == e)((e, updatedQueue)))

    remaining match
      case None => (donationPart.map((earmarking, _)), updatedAccount)
      case Some(r) =>
        val (donationPart2, updatedAccount2) = updatedAccount.pull(r)
        (donationPart.map((earmarking, _)) ++ donationPart2, updatedAccount2)

  private def findQueueWithOldestDonation(using donationGetter: DonationGetter): (Option[Earmarking], DonationQueue) =
    val allQueues = (None, unboundDonations) +: boundDonations.map(t => (Some(t._1), t._2))
    allQueues
      .filter(_._2.donationQueue._2.nonEmpty)
      .minByOption(_._2.donationQueue._2.head.value.donation.get.donationDate)
      // TODO: change to actual error handling
      .getOrElse(throw new IllegalStateException("No donations available in any queue"))
