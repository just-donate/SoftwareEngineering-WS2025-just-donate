package com.just.donate.notify.messages;

import com.just.donate.models.Donor
import com.just.donate.config.Config

case class ForgotTrackingIdMessage(val donor: Donor, config: Config) extends MessageType:
  val defaultTemplate: String =
    """You have requested to have your tracking id sent again.
      |To track your progress visit
      |{{tracking-link-with-id}}
      |or enter your tracking id
      |{{tracking-id}}
      |on our tracking page
      |{{tracking-link}}""".stripMargin

  val replacements: Seq[(String, String)] = Seq(
    ("tracking-id", donor.id),
    ("tracking-link", config.frontendUrl),
    (
      "tracking-link-with-id",
      f"${config.frontendUrl}/tracking?id=${donor.id}"
    )
  )
