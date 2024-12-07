package com.just.donate.models

import com.just.donate.utils.ReservableQueue

case class Account(
  name: String,
  private var boundDonations: Seq[(String, DonationQueue)] = Seq.empty,
  private var unboundDonations: DonationQueue = null,
  private var incomingFlow: Seq[Account] = Seq.empty,
  private var outgoingFlow: Seq[Account] = Seq.empty
):

  unboundDonations = DonationQueue(this, ReservableQueue(this))
  
  def addIncomingFlow(account: Account): Unit =
    if account == this then
      throw new IllegalArgumentException("Account cannot have incoming models from itself"
      )

    if ! incomingFlow.contains(account) then
      incomingFlow = incomingFlow :+ account
      account.addOutgoingFlow(this)

  protected def addOutgoingFlow(account: Account): Unit =
    if account == this then
      throw new IllegalArgumentException("Account cannot have outgoing models to itself")

    if ! outgoingFlow.contains(account) then
      outgoingFlow = outgoingFlow :+ account
      account.addIncomingFlow(this)

  def donate(donor: String, amount: Nothing): Unit =
    val donation: Donation = new Donation(donor, amount)
    unboundDonations.addAll(donation.parts)

  def donate(donation: Donation): Unit =
    unboundDonations.addAll(donation.parts)

  def donate(donation: Donation, earmarking: String): Boolean =
    val boundQueue: Option[DonationQueue] = getBoundQueue(earmarking)
    if boundQueue.isDefined then
      boundQueue.get.addAll(donation.parts)
      true
    else false

  def donate(donor: String, amount: Nothing, boundTo: String): Boolean =
    val boundQueue: Option[DonationQueue] = getBoundQueue(boundTo)
    if boundQueue.isDefined then
      val donation: Donation = new Donation(donor, amount)
      boundQueue.get.addAll(donation.parts)
      true
    else false

  protected def spend(expense: Expense): Boolean =
    // If the total balance is less than the expense, we cannot spend it
    if totalBalance < expense.amount then
      return false
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

  private def totalBalanceUnbound: BigDecimal = unboundDonations.totalBalance

  private def getUpstreamBalance(earmarking: String): BigDecimal =
    var balance = getBoundQueue(earmarking).map(_.totalBalance).getOrElse(BigDecimal(0))
    for account <- incomingFlow do balance = balance + (account.getUpstreamBalance(earmarking))
    balance

  def addEarmarking(earmarking: String): Unit =
    boundDonations = boundDonations :+ ((earmarking, DonationQueue(this, ReservableQueue(this))))

  private def getBoundQueue(earmarking: String): Option[DonationQueue] = boundDonations
    .filter(_._1 == earmarking)
    .map(_._2)
    .headOption

  private[models] def getUnboundDonations: DonationQueue = unboundDonations

  private[models] def getBoundDonations: Seq[(String, DonationQueue)] = boundDonations

  private def totalBalanceBound: BigDecimal = boundDonations.map(_._2.totalBalance).sum

  def totalBalance: BigDecimal = totalBalanceUnbound + totalBalanceBound

  def totalEarmarkedBalance(earmarking: String): BigDecimal =
    getBoundQueue(earmarking).map(_.totalBalance).getOrElse(BigDecimal(0))
