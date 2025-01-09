package com.just.donate.models

import java.time.LocalDateTime

case class StatusUpdate(
  date: LocalDateTime,
  status: StatusUpdate.Status,
  description: String
)

object StatusUpdate:

  enum Status(message: String):
    case ANNOUNCED extends Status("The donation has been announced.")
    case PENDING_CONFIRMATION extends Status("The donation is pending confirmation.")
    case CONFIRMED extends Status("The donation has been confirmed.")
    case RECEIVED extends Status("The donation has been received.")
    case IN_TRANSFER extends Status("The donation is in transfer.")
    case PROCESSING extends Status("The donation is being processed.")
    case ALLOCATED extends Status("The donation has been allocated.")
    case AWAITING_UTILIZATION extends Status("The donation is awaiting utilization.")
    case USED extends Status("The donation has been used.")
