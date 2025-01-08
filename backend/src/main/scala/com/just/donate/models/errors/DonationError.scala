package com.just.donate.models.errors

enum DonationError(val message: String):
  case INVALID_ACCOUNT extends DonationError("Account not found")
  case INVALID_EARMARKING extends DonationError("Earmarking not found")
