package com.just.donate.notify

import cats.effect.IO

object SendEmail:

  def sendEmail(donor: String, message: String = "Thank you for your donation"): IO[Unit] =
    IO.println(s"Sending email to $donor with message: $message")
