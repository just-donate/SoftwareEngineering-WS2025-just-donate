package com.just.donate.models.errors

enum WithdrawError(val message: String):
  case INVALID_ACCOUNT extends WithdrawError("Account not found")
  case INSUFFICIENT_ACCOUNT_FUNDS extends WithdrawError("Source account has insufficient funds")
  case INVALID_EARMARKING extends WithdrawError("Earmarking not found")
  case INVALID_DONOR extends WithdrawError("Donor not found")
