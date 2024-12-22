package com.just.donate.models

import com.just.donate.utils.structs.ReservableQueue
import com.just.donate.utils.CollectionUtils.{updatedReturn, map1, map2}



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
    val (updatedFrom, donationParts) = boundDonations.updatedReturn(b => b._1 == earmarking)(b => b._2.pull(amount).map2(q => (b._1, q)))
    donationParts match
      case Some(donationParts) => (donationParts, copy(boundDonations = updatedFrom))
      case None => throw new IllegalArgumentException(s"Earmarking $earmarking does not exist")

  private[models] def pull(amount: BigDecimal): (BigDecimal, DonationPart, Option[String], Account) =
    if totalBalance < amount then throw new IllegalArgumentException(s"Account $name has insufficient funds")

    val oldestDonation =
      (("", unboundDonations) +: boundDonations).minBy(_._2.donationQueue._2.head.value.donation.donationDate)
    
    ???
  
  private[models] def push(donation: DonationPart, earmarking: Option[String]): Account =
    earmarking match
      case Some(earmark) => donate(donation, earmark)._2
      case None          => donate(donation)._2

  def totalBalance: BigDecimal = totalBalanceUnbound + totalBalanceBound

  private def totalBalanceUnbound: BigDecimal = unboundDonations.totalBalance

  private def totalBalanceBound: BigDecimal = boundDonations.map(_._2.totalBalance).sum
