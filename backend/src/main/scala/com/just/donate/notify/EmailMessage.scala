package com.just.donate.notify

import com.just.donate.config.Config
import com.just.donate.models.Donor
import com.just.donate.notify.messages.MessageType;

object EmailMessage:
  def prepareString(
    template: Option[String],
    messageType: MessageType
  ): String =
    messageType.replacements
      .foldLeft(template.getOrElse(messageType.defaultTemplate))((acc, replacement) =>
        acc.replaceAll(
          f"([^\\\\])\\{\\{${replacement._1}\\}\\}",
          "$1" + replacement._2
        )
      )
      .replaceAll("\\\\(\\{\\{)", "$1")

case class EmailMessage(targetAddress: String, message: String, subject: String)
