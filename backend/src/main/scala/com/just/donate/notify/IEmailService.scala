package com.just.donate.notify

import cats.effect.IO

trait IEmailService:
  def sendEmail(donor: String, message: String, subject: String = "Just Donate: Thank you for your donation"): IO[Unit]
