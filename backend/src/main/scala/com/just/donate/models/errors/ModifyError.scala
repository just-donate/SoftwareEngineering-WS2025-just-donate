package com.just.donate.models.errors

enum ModifyError(val message: String):
  case INVALID_ACCOUNT extends ModifyError("Account not found")
  case INVALID_EARMARKING extends ModifyError("Earmarking not found")
  case EARMARKING_HAS_BUDGET extends ModifyError("Earmarking has budget")
