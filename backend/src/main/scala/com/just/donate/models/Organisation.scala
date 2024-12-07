package com.just.donate.models

case class Organisation(
                         name: String,
                         private var accounts: Seq[Account] = Seq.empty
                       ):

  def getAccount(name: String): Account = accounts.find(_.name == name)
      .getOrElse(throw new IllegalArgumentException(s"Account $name not found"))

  def addAccount(name: String): Unit = addAccount(Account(name))

  def addAccount(account: Account): Unit = Organisation(name, accounts :+ account)

  def addEarmarking(earmarking: String): Unit = 
    accounts.foreach(_.addEarmarking(earmarking))

  def totalBalance: BigDecimal = accounts.map(_.totalBalance).sum

  def totalEarmarkedBalance(earmarking: String): BigDecimal = accounts.map(_.totalEarmarkedBalance(earmarking)).sum


