package com.just.donate.models

import java.time.LocalDateTime

case class StatusUpdate(
  date: LocalDateTime,
  status: StatusUpdate.Status,
  description: String
)

object StatusUpdate:

  enum Status:
    case DONATED
    case PENDING
    case IN_PROGRESS
    case COMPLETE
