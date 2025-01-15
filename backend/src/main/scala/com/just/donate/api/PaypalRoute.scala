package com.just.donate.api

import cats.data.Chain
import cats.effect.*
import com.just.donate.api.DonationRoute.{RequestDonation, organisationMapper}
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.models.paypal.{PayPalIPN, PayPalIPNMapper}
import com.just.donate.notify.IEmailService
import com.just.donate.utils.ErrorLogger
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

  val paypalAccountName = "PayPal"

  def paypalRoute(paypalRepo: Repository[String, PayPalIPN], orgRepo: Repository[String, Organisation], conf: Config, emailService: IEmailService, errorLogger: ErrorLogger): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@POST -> Root =>
      for

        /**
         * Parse the URL form data and the raw body
         */
        urlForm <- req.as[UrlForm].map(_.values)
        rawBody <- req.bodyText.compile.string

        _ <- IO.println(s"Received IPN: $rawBody")

        /**
         * Respond to PayPal immediately to avoid retries
         */
        response <- Ok("").handleErrorWith { error =>
          IO.println(s"Failed to send immediate response to PayPal: $error") >> InternalServerError(
            "Error processing IPN"
          )
        }

        /**
         * Process the IPN
         */
        _ <- processIpn(rawBody, urlForm, paypalRepo, orgRepo, conf, emailService, errorLogger).start
      yield response
  }

  /**
   * Process an IPN from PayPal
   *
   * @param rawBody      The raw IPN body
   * @param urlForm      The parsed URL form data
   * @param paypalRepo   The PayPal IPN repository
   * @param orgRepo      The organisation repository
   * @param conf         The application configuration
   * @param emailService The email service
   * @param errorLogger  The error logger
   * @return An IO action that processes the IPN
   */
  private def processIpn(
                          rawBody: String,
                          urlForm: Map[String, Chain[String]],
                          paypalRepo: Repository[String, PayPalIPN],
                          orgRepo: Repository[String, Organisation],
                          conf: Config,
                          emailService: IEmailService,
                          errorLogger: ErrorLogger
                        ): IO[Unit] = {
    validateWithRetry(rawBody, maxRetries = 3, delay = 5.seconds).flatMap {
      case "VERIFIED" =>
        for
          _ <- IO.println("IPN verified by PayPal")

          /**
           * Map the IPN data to a PayPal IPN
           */
          newIpn <- PayPalIPNMapper.mapToPayPalIPN(urlForm).handleErrorWith { error =>
            val errMsg = s"Failed to map IPN: $error"
            IO.println(errMsg) >>
              errorLogger.logError("IPN", errMsg, rawBody) >>
              IO.raiseError(error)
          }

          /**
           * Check if the payment status is "Completed"
           */
          _ <- if (newIpn.paymentStatus.equals("Completed")) {
            IO.unit
          } else {
            val errMsg = s"Invalid payment status: ${newIpn.paymentStatus}. Expected: Completed"
            IO.println(errMsg) >>
              errorLogger.logError("IPN", errMsg, rawBody) >>
              IO.raiseError(new IllegalArgumentException("Invalid payment status"))
          }

          /**
           * Check if the IPN is a duplicate
           */
          existingIpn <- paypalRepo.findById(newIpn.ipnTrackId)
          _ <- existingIpn match {
            case Some(_) =>
              val errMsg = s"Duplicate IPN detected for IPN track ID: ${newIpn.ipnTrackId}"
              IO.println(errMsg) >>
                errorLogger.logError("IPN", errMsg, rawBody) >>
                IO.raiseError(new IllegalStateException("Duplicate IPN detected"))
            case None => IO.unit
          }

          /**
           * Save the IPN to the database
           */
          _ <- paypalRepo.save(newIpn).handleErrorWith { error =>
            val errMsg = s"Failed to save IPN: $error"
            IO.println(errMsg) >>
              errorLogger.logError("IPN", errMsg, rawBody) >>
              IO.raiseError(error)
          }

          /**
           * Map the IPN data to a request donation
           */
          requestDonation <- IO.pure(
            RequestDonation(
              newIpn.firstName + " " + newIpn.lastName,
              newIpn.notificationEmail,
              newIpn.mcGross,
              if (newIpn.itemName.isEmpty || newIpn.itemName == "__empty__") None else Some(newIpn.itemName)
            )
          )

          /**
           * Map the IPN data to an organisation and save it
           */
          donationResult <- loadAndSaveOrganisationOps(math.abs(newIpn.organisationName.hashCode).toString)(orgRepo)(
            organisationMapper(requestDonation, paypalAccountName, conf)
          )

          _ <- donationResult match {
            case None =>
              errorLogger.logError("IPN", "No Tracking ID generated", rawBody) >>
                IO.println("No Tracking ID generated")
            case Some(Left(donationError)) =>
              errorLogger.logError("IPN", donationError.message, rawBody) >>
                IO.println(donationError.message)
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
          }

        yield ()
      case "INVALID" =>
        IO.println("IPN invalid")
      case other =>
        IO.println(s"Unexpected validation response: $other")
    }
  }

  /**
   * Validate an IPN with PayPal, retrying up to `maxRetries` times with a delay of `delay` between each attempt
   *
   * @param rawBody    The raw IPN body
   * @param maxRetries The maximum number of retries
   * @param delay      The delay between retries
   * @return An IO action that validates the IPN
   */
  private def validateWithRetry(rawBody: String, maxRetries: Int, delay: FiniteDuration): IO[String] =
    val validationPayload = s"cmd=_notify-validate&$rawBody"

    var paypalValidationUrl = System.getProperty("PAYPAL_VALIDATION_URL")

    if (paypalValidationUrl == null) {
      paypalValidationUrl = sys.env.getOrElse(
        "PAYPAL_VALIDATION_URL",
        "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr"
      )
    }

    /**
     * Perform the HTTP request to validate the IPN with PayPal
     */
    def doRequest: IO[String] = IO {
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
            line = reader.readLine();
            line != null
          } do response.append(line)
          response.toString
        finally reader.close()
      finally connection.disconnect()
    }

    /**
     * Attempt to validate the IPN with PayPal, retrying up to `maxRetries` times
     *
     * @param attemptNumber The current attempt number
     */
    def attempt(attemptNumber: Int): IO[String] =
      doRequest.attempt.flatMap {
        case Right(response) =>
          IO.pure(response)
        case Left(error) if attemptNumber < maxRetries =>
          IO.println(s"Validation attempt $attemptNumber failed: $error. Retrying...") >>
            Temporal[IO].sleep(delay) >>
            attempt(attemptNumber + 1)
        case Left(error) =>
          IO.println(s"Validation failed after $maxRetries attempts: $error") >>
            IO.raiseError[String](error)
      }

    attempt(1)
