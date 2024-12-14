package com.just.donate.models

import com.just.donate.utils.structs.ReservableQueue

case class Account private (
  name: String,
  boundDonations: Seq[(String, DonationQueue)],
  unboundDonations: DonationQueue
):

  def this(name: String) = this(name, Seq.empty, DonationQueue(name, ReservableQueue(name)))

  def donate(donor: String, amount: BigDecimal): (Boolean, Account) =
    val donation: DonationPart = Donation(donor, amount)
    donate(donation)

  def donate(donation: DonationPart): (Boolean, Account) =
    (true, copy(unboundDonations = unboundDonations.add(donation)))

  def donate(donor: String, amount: BigDecimal, earmarking: String): (Boolean, Account) =
    donate(Donation(donor, amount, earmarking), earmarking)

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

  def removeEarmarking(earmarking: String): Account =
    ???

  def addEarmarking(earmarking: String): Account =
    // boundDonations = boundDonations :+ ((earmarking, DonationQueue(name, ReservableQueue(name))))
    copy(boundDonations = boundDonations :+ ((earmarking, DonationQueue(name, ReservableQueue(name)))))

  def totalEarmarkedBalance(earmarking: String): BigDecimal =
    getBoundQueue(earmarking).map(_.totalBalance).getOrElse(BigDecimal(0))

  private def getBoundQueue(earmarking: String): Option[DonationQueue] =
    boundDonations.filter(_._1 == earmarking).map(_._2).headOption

  protected def spend(expense: Expense): Boolean =
    // If the total balance is less than the expense, we cannot spend it
    if totalBalance < expense.amount then return false
    if expense.isBound then spendBound(expense)
    else spendUnbound(expense)

  private def spendUnbound(expense: Expense): Boolean =
    // 1. Expense is unbound, withdraw from unbound donations
    //    - if unbound are not enough, go into minus as long it is covered by bound donations
    //      (we don't need to subtract form bound, as they are reserved and the organization
    //       must cover the expense from unbound later on)
    // 2. Expense is unbound, but not enough unbound donations, return false
    val remainingExpense: Expense = spendUnboundFromAccount(expense)
    if remainingExpense.isPaid then return true
    // Now we need to reserve upstream
    // TODO: Implement this
    false

  private def spendUnboundFromAccount(expense: Expense): Expense =
    val polled = this.unboundDonations.donationQueue.pollUnreserved(expense.amount)
    polled._1.foreach(expense.payWith)
    expense

  private def spendBound(expense: Expense): Boolean =
    // 1. Expense is bound, withdraw from bound donations
    //    - if bound are not enough, check if up the queue are more and reserve them, go into minus
    //      as long it is covered by unbound donations
    // 2. Expense is bound, but not enough bound donations up queue, withdraw from unbound donations.
    //    - if unbound are not enough, do not go into minus, as an account must be covered, return false
    val earmarking: String = expense.earMarking.get
    // TODO: Implement this
    false

  def totalBalance: BigDecimal = totalBalanceUnbound + totalBalanceBound

  private def totalBalanceUnbound: BigDecimal = unboundDonations.totalBalance

  private def totalBalanceBound: BigDecimal = boundDonations.map(_._2.totalBalance).sum
