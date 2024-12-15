package com.just.donate.models

case class Organisation(name: String, accounts: Seq[Account] = Seq.empty):

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

  def donate(donor: String, amount: BigDecimal, earmarking: Option[String], account: String): Organisation =
    val donation = Donation(donor, amount)
    accounts.find(_.name == account) match
      case Some(acc) =>
        val (donated, newAcc) = earmarking match
          case Some(earmark) => acc.donate(donation, earmark)
          case None          => acc.donate(donation)
        copy(accounts = accounts.map(a => if a.name == account then newAcc else a))
      case None => this

  def withdrawal(amount: BigDecimal, account: String, earmarking: Option[String]): Organisation =
    getAccount(account) match
      case Some(value) =>
        val expense = Expense("", amount, earmarking)
        copy(accounts = accounts.map(a => if a.name == account then value.withdrawal(expense) else a))
      case None => throw new IllegalArgumentException(s"Account $account does not exist")

  def transfer(amount: BigDecimal, fromAccount: String, toAccount: String): Organisation =
    (getAccount(fromAccount), getAccount(toAccount)) match
      case (Some(from), Some(to)) => transfer(amount, from, to)
      case _ => throw new IllegalArgumentException(s"Account $fromAccount or $toAccount does not exist")

  def transfer(amount: BigDecimal, fromAccount: Account, toAccount: Account): Organisation =
    if fromAccount.totalBalance < amount then
      throw new IllegalArgumentException(s"Account ${fromAccount.name} has insufficient funds")
      
    val (remaining, donationPart, earmarked, updatedFrom) = fromAccount.pull(amount)
    val updatedTo = toAccount.push(donationPart, earmarked)

    val updatedOrg = copy(accounts =
      accounts.map(a =>
        if a.name == fromAccount.name then updatedFrom
        else if a.name == toAccount.name then updatedTo
        else a
      )
    )

    if remaining == BigDecimal(0) then updatedOrg
    else updatedOrg.transfer(remaining, fromAccount, toAccount)

  def totalBalance: BigDecimal =
    accounts.map(_.totalBalance).sum

  def totalEarmarkedBalance(earmarking: String): BigDecimal =
    accounts.map(_.totalEarmarkedBalance(earmarking)).sum
