package com.just.donate.api

import cats.effect.*
import com.just.donate.db.mongo.MongoPaypalRepository
import com.just.donate.models.paypal.PayPalIPNMapper
import io.circe.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import org.http4s.dsl.io.*

import java.io.{BufferedReader, DataOutputStream, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import scala.concurrent.duration.*

object PaypalRoute:

  def paypalRoute(repo: MongoPaypalRepository, client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
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
              newIpn <- PayPalIPNMapper.mapToPayPalIPN(urlForm).handleErrorWith { error =>
                IO.println(s"Failed to map IPN: $error") *> IO.raiseError(error)
              }
              // TODO: Additional verification checks (recipient email, amount, duplicate, etc.)
              _ <- repo.save(newIpn).handleErrorWith { error =>
                IO.println(s"Failed to save IPN: $error")
              }
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
    val paypalValidationUrl = sys.env.getOrElse(
      "PAYPAL_VALIDATION_URL",
      "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr"
    )

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
