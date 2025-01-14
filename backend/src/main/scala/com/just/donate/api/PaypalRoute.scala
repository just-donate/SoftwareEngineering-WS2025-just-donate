package com.just.donate.api

import cats.effect.*
import com.just.donate.api.DonationRoute.{RequestDonation, organisationMapper}
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.models.paypal.{PayPalIPN, PayPalIPNMapper}
import com.just.donate.utils.RouteUtils.loadAndSaveOrganisationOps
import com.just.donate.notify.IEmailService
import com.just.donate.notify.EmailMessage
import com.just.donate.notify.messages.DonationMessage
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io.*

import java.io.{BufferedReader, DataOutputStream, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import scala.concurrent.duration.*

object PaypalRoute:

  def paypalRoute(paypalRepo: Repository[String, PayPalIPN], orgRepo: Repository[String, Organisation], conf: Config, emailService: IEmailService): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root =>
      for
        // Parse the request body
        urlForm <- req.as[UrlForm].map(_.values)
        rawBody <- req.bodyText.compile.string

        _ <- IO.println(s"Received IPN: $rawBody")

        // Immediately respond to PayPal to avoid IPN timeouts
        response <- Ok("").handleErrorWith { error =>
          IO.println(s"Failed to send immediate response to PayPal: $error") *> InternalServerError(
            "Error processing IPN"
          )
        }

        // Validate IPN asynchronously with retries
        _ <- validateWithRetry(rawBody, maxRetries = 3, delay = 5.seconds).flatMap {
          case "VERIFIED" =>
            for
              _ <- IO.println("IPN verified by PayPal")

              // Map the incoming IPN data to a domain model
              newIpn <- PayPalIPNMapper.mapToPayPalIPN(urlForm).handleErrorWith { error =>
                IO.println(s"Failed to map IPN: $error") *> IO.raiseError(error)
              }

              // Verify recipient email
//              _ <- if (newIpn.receiverEmail == "expected-business-email@example.com") {
//                IO.unit
//              } else {
//                IO.println(s"Invalid recipient email: ${newIpn.receiverEmail}. Expected: expected-business-email@example.com") *>
//                  IO.raiseError(new IllegalArgumentException("Invalid recipient email"))
//              }

              // Check whether the payment status is completed
              _ <- if (newIpn.paymentStatus.equals("Completed")) {
                IO.unit
              } else {
                IO.println(s"Invalid payment status: ${newIpn.receiverEmail}. Expected: Completed") *>
                                  IO.raiseError(new IllegalArgumentException("Invalid recipient email"))
              }

              // Check for duplicates
              existingIpn <- paypalRepo.findById(newIpn.ipnTrackId)
              _ <- existingIpn match {
                case Some(_) =>
                  IO.println(s"Duplicate IPN detected for IPN track ID: ${newIpn.ipnTrackId}") *>
                    IO.raiseError(new IllegalStateException("Duplicate IPN detected"))
                case None => IO.unit // No duplicate found, continue processing
              }

              // Save the IPN if all checks pass
              _ <- paypalRepo.save(newIpn).handleErrorWith { error =>
                IO.println(s"Failed to save IPN: $error") *> IO.raiseError(error)
              }

              // Call the donation endpoint
              // Item name is set by the donation-paypal.html as earmarking
              requestDonation <- IO.pure(RequestDonation(newIpn.firstName, newIpn.payerEmail, newIpn.mcGross, if newIpn.itemName.isEmpty then None else Option[String](newIpn.itemName)))
              donationResult <- loadAndSaveOrganisationOps(math.abs(newIpn.organisationName.hashCode).toString)(orgRepo)(
                organisationMapper(requestDonation, "Paypal")
              )
              _ <- donationResult match
                case None                      => BadRequest("Organisation not found")
                case Some(Left(donationError)) => BadRequest(donationError.message)
                case Some(Right((org, donor))) =>
                  emailService.sendEmail(
                    requestDonation.donorEmail,
                    EmailMessage.prepareString(
                      org.theme.map(_.emailTemplates.donationTemplate),
                      DonationMessage(
                        donor,
                        conf,
                      )
                    )
                  )

            yield ()
          case "INVALID" =>
            IO.println("IPN invalid")
          case other =>
            IO.println(s"Unexpected validation response: $other")
        }.start // Run validation asynchronously
      yield response
  }

  // Function to validate IPN with PayPal, adding retry logic
  private def validateWithRetry(rawBody: String, maxRetries: Int, delay: FiniteDuration): IO[String] =
    val validationPayload = s"cmd=_notify-validate&$rawBody"

    var paypalValidationUrl = System.getProperty("PAYPAL_VALIDATION_URL")

    if (paypalValidationUrl == null) {
      paypalValidationUrl = sys.env.getOrElse(
        "PAYPAL_VALIDATION_URL",
        "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr"
      )
    }

    def doRequest: IO[String] = IO.blocking {
      // Create URL and open connection
      val url = new URL(paypalValidationUrl)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      try
        // Configure connection for POST
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.setRequestProperty("User-Agent", "Scala-IPN-Verification-Script")

        // Write the payload to the connection output stream
        val outputStream = new DataOutputStream(connection.getOutputStream)
        try
          outputStream.writeBytes(validationPayload)
          outputStream.flush()
        finally outputStream.close()

        // Read the response (if response code indicates error, read error stream)
        val responseCode = connection.getResponseCode
        val inputStream =
          if responseCode >= HttpURLConnection.HTTP_BAD_REQUEST then connection.getErrorStream
          else connection.getInputStream

        val reader = new BufferedReader(new InputStreamReader(inputStream))
        try
          val response = new StringBuilder
          var line: String = null
          while {
            line = reader.readLine(); line != null
          } do response.append(line)
          response.toString
        finally reader.close()
      finally connection.disconnect()
    }

    def attempt(attemptNumber: Int): IO[String] =
      doRequest.attempt.flatMap {
        case Right(response) =>
          IO.pure(response)
        case Left(error) if attemptNumber < maxRetries =>
          IO.println(s"Validation attempt $attemptNumber failed: $error. Retrying...") *>
            Temporal[IO].sleep(delay) *>
            attempt(attemptNumber + 1)
        case Left(error) =>
          IO.println(s"Validation failed after $maxRetries attempts: $error") *>
            IO.raiseError[String](error)
      }

    attempt(1)
