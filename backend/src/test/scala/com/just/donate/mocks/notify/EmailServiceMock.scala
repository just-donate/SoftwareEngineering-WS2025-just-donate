package com.just.donate.mocks.notify

import cats.effect.IO
import com.just.donate.notify.IEmailService

class EmailServiceMock extends IEmailService:
  override def sendEmail(donor: String, message: String, subject: String): IO[Unit] = IO.unit