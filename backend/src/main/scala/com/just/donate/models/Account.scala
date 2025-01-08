package com.just.donate.models

import com.just.donate.models.Types.DonationGetter
import com.just.donate.utils.CollectionUtils.{map2, updated, updatedReturn}
import com.just.donate.utils.structs.ReservableQueue

case class Account private (
  name: String,
  boundDonations: Seq[(String, DonationQueue)],
  unboundDonations: DonationQueue
):

  def this(name: String) = this(name, Seq.empty, DonationQueue(name, ReservableQueue(name)))

  def removeEarmarking(earmarking: String): Account =
    boundDonations.find(_._1 == earmarking) match
      case Some(value) =>
        if value._2.totalBalance == BigDecimal.valueOf(0) then
          copy(boundDonations = boundDonations.filterNot(_._1 == earmarking))
        else throw new IllegalArgumentException(s"Earmarking $earmarking has a balance of ${value._2.totalBalance}")
      case None => this

  def addEarmarking(earmarking: String): Account =
    // boundDonations = boundDonations :+ ((earmarking, DonationQueue(name, ReservableQueue(name))))
    copy(boundDonations = boundDonations :+ ((earmarking, DonationQueue(name, ReservableQueue(name)))))

  def totalEarmarkedBalance(earmarking: String): BigDecimal =
    getBoundQueue(earmarking).map(_.totalBalance).getOrElse(BigDecimal(0))

  private def getBoundQueue(earmarking: String): Option[DonationQueue] =
    boundDonations.filter(_._1 == earmarking).map(_._2).headOption

  def withdrawal(amount: BigDecimal, earmarking: Option[String]): (Seq[DonationPart], Account) =
    // If the total balance is less than the expense, we cannot spend it
    // TODO: change to actual error handling
    if totalBalance < amount then throw new IllegalArgumentException(s"Account $name has insufficient funds")
    if earmarking.isDefined then withdrawalBound(amount, earmarking.get)
    else withdrawalUnbound(amount)

  private def withdrawalUnbound(amount: BigDecimal): (Seq[DonationPart], Account) =
    // 1. Expense is unbound, withdraw from unbound donations
    //    - if unbound are not enough, go into minus as long it is covered by bound donations
    //      (we don't need to subtract form bound, as they are reserved and the organization
    //       must cover the expense from unbound later on)
    // 2. Expense is unbound, but not enough unbound donations, return false

    // TODO: For now a simple implementation, where we just pull the amount from the unbound donations
    val (donationParts, updatedFrom) = this.unboundDonations.pull(amount)
    (donationParts, copy(unboundDonations = updatedFrom))

  private def withdrawalBound(amount: BigDecimal, earmarking: String): (Seq[DonationPart], Account) =
    // 1. Expense is bound, withdraw from bound donations
    //    - if bound are not enough, check if up the queue are more and reserve them, go into minus
    //      as long it is covered by unbound donations
    // 2. Expense is bound, but not enough bound donations up queue, withdraw from unbound donations.
    //    - if unbound are not enough, do not go into minus, as an account must be covered, return false

    // TODO: For now a simple implementation, where we just pull the amount from the bound donations
    val (updatedFrom, donationParts) =
      boundDonations.updatedReturn(b => b._1 == earmarking)(b => b._2.pull(amount).map2(q => (b._1, q)))
    donationParts match
      case Some(donationParts) => (donationParts, copy(boundDonations = updatedFrom))
      case None                => throw new IllegalArgumentException(s"Earmarking $earmarking does not exist")

  private[models] def pull(amount: BigDecimal)(using
    donationGetter: DonationGetter
  ): (BigDecimal, DonationPart, Option[String], Account) =
    // TODO: change to actual error handling
    if totalBalance < amount then throw new IllegalArgumentException(s"Account $name has insufficient funds")

    val (earmarking, queue) = findQueueWithOldestDonation
    val (donationPart, updatedQueue) = queue.pull(amount, Some(1))

    val updatedAccount = earmarking match
      case None    => copy(unboundDonations = updatedQueue)
      case Some(e) => copy(boundDonations = boundDonations.updated(b => b._1 == e)((e, updatedQueue)))

    (amount - donationPart.head.amount, donationPart.head, earmarking, updatedAccount)

  private def findQueueWithOldestDonation(using donationGetter: DonationGetter): (Option[String], DonationQueue) =
    val allQueues = (None, unboundDonations) +: boundDonations.map(t => (Some(t._1), t._2))
    allQueues
      .filter(_._2.donationQueue._2.nonEmpty)
      .minByOption(_._2.donationQueue._2.head.value.donation.get.donationDate)
      // TODO: change to actual error handling
      .getOrElse(throw new IllegalStateException("No donations available in any queue"))

  def totalBalance: BigDecimal = totalBalanceUnbound + totalBalanceBound

  private def totalBalanceUnbound: BigDecimal = unboundDonations.totalBalance

  private def totalBalanceBound: BigDecimal = boundDonations.map(_._2.totalBalance).sum

  private[models] def push(donation: DonationPart, earmarking: Option[String]): Account =
    earmarking match
      case Some(earmark) => donate(donation, earmark)._2
      case None          => donate(donation)._2

  def donate(donation: DonationPart): (Boolean, Account) =
    (true, copy(unboundDonations = unboundDonations.add(donation)))

  // TODO: use error instead of bool
  def donate(donation: DonationPart, earmarking: String): (Boolean, Account) =
    def donateRec(queues: Seq[(String, DonationQueue)]): (Boolean, Seq[(String, DonationQueue)]) = queues match
      case Nil => (false, Nil)
      case (earmarkingQueue, queue) :: tail =>
        if earmarking == earmarkingQueue then (true, (earmarkingQueue, queue.add(donation)) +: tail)
        else
          val (donated, newQueues) = donateRec(tail)
          (donated, (earmarkingQueue, queue) +: newQueues)

    val (donated, newBoundDonations) = donateRec(boundDonations)
    if donated then (true, copy(boundDonations = newBoundDonations))
    else (false, this)
