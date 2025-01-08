package com.just.donate.models.errors

enum TransferError(val message: String):
  case INVALID_ACCOUNT extends TransferError("Account not found")
  case INSUFFICIENT_ACCOUNT_FUNDS extends TransferError("Source account has insufficient funds")
  case NON_POSITIVE_AMOUNT extends TransferError("Amount has to be positive")
  case SAME_SOURCE_AND_DESTINATION_ACCOUNT extends TransferError("The source and target accounts are the same")
  case INVALID_DONOR extends TransferError("Donor not found")
