package com.just.donate.notify.messages;

import com.just.donate.models.Donor
import com.just.donate.config.Config
import com.just.donate.models.Organisation

case class WithdrawalMessage(val donor: Donor, config: Config, organisation: Organisation) extends MessageType:
  val defaultTemplate: String =
    """Your recent donation to {{organisation-name}} has been fully utilized.
      |To see more details about the status of your donation, visit the following link
      |{{tracking-link-with-id}}
      |or enter your tracking id
      |{{tracking-id}}
      |on our tracking page
      |{{tracking-link}}""".stripMargin

  val replacements: Seq[(String, String)] = Seq(
    ("tracking-id", donor.id),
    ("tracking-link", config.frontendUrl),
    ("tracking-link-with-id", f"${config.frontendUrl}/tracking?id=${donor.id}"),
    ("organisation-name", organisation.name)
  )
