package com.just.donate.api.helper

import com.just.donate.api.OrganisationRoute.{ResponseAccount, ResponseEarmarking}
import com.just.donate.utils.Money

trait ApiAction[R]

object ApiAction:

  case class AddEarmarking(name: String, description: String) extends ApiAction[Unit]

  case class ListEarmarkings() extends ApiAction[Seq[ResponseEarmarking]]

  case class AddBankAccount(name: String, initialBalance: Money) extends ApiAction[Unit]

  case class ListBankAccounts() extends ApiAction[Seq[ResponseAccount]]