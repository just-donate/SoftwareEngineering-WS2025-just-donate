package com.just.donate.notify

import cats.effect.IO
import com.just.donate.config.AppConfig

import java.util.Properties
import javax.mail.*
import javax.mail.internet.*

class SendEmail(val config: AppConfig):
  private val mailServerProperties = {
    val properties = new Properties()
    properties.put("mail.smtp.host", config.mailSmtpHost)
    properties.put("mail.smtp.port", config.mailSmtpPort)
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")
    properties
  }

  def sendEmail(donor: String, message: String, subject: String = "Just Donate: Thank you for your donation"): IO[Unit] =
    for {
      _ <- IO.println(s"Sending email to $donor")
      session <- IO(Session.getInstance(mailServerProperties, new Authenticator() {
        // TODO: choose a more secure authentication method
        override def getPasswordAuthentication: PasswordAuthentication = {
          new PasswordAuthentication(config.mailAddress, config.mailPassword)
        }
      }))
      mailMessage <- IO({
        val mailMessage = new MimeMessage(session)
        mailMessage.setFrom(new InternetAddress(config.mailAddress))
        mailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(donor))
        mailMessage.setSubject(subject)
        mailMessage.setText(message)
        mailMessage
      })
      _ <- IO(Transport.send(mailMessage)).attempt.map {
        case Left(error) =>
          Console.err.println(s"Error while sending mail:")
          error.printStackTrace(Console.err)
        case Right(value) =>
          println("Email sent successfully")
      }
    } yield {}