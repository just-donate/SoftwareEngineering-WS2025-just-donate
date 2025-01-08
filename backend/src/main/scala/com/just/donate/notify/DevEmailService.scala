package com.just.donate.notify

import cats.effect.IO
import com.just.donate.config.Config

import java.util.Properties
import javax.mail.*
import javax.mail.internet.*

class DevEmailService(val config: Config) extends IEmailService:
  def sendEmail(
    email: String,
    message: String,
    subject: String = "Just Donate: Thank you for your donation"
  ): IO[Unit] =
    IO.println(
      s"""Sending email to $email:
         |Subject: ${subject}
         |${message}""".stripMargin
    )
