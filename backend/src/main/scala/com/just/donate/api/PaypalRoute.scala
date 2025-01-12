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
import org.typelevel.ci.CIString

import scala.concurrent.duration.*


object PaypalRoute:

  def paypalRoute(repo: MongoPaypalRepository, client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@POST -> Root =>
      for {
        // Parse the request body
        urlForm <- req.as[UrlForm].map(_.values)
        rawBody <- req.bodyText.compile.string
        
        _ <- IO.println(s"Received IPN: $rawBody")

        // Immediately respond to PayPal to avoid IPN timeouts
        response <- Ok("").handleErrorWith { error =>
          IO.println(s"Failed to send immediate response to PayPal: $error") *> InternalServerError("Error processing IPN")
        }

        // Validate IPN asynchronously with retries
        _ <- validateWithRetry(client, rawBody, maxRetries = 3, delay = 5.seconds).flatMap {
          case "VERIFIED" =>
            for {
              _ <- IO.println("IPN verified by PayPal")
              newIpn <- PayPalIPNMapper.mapToPayPalIPN(urlForm).handleErrorWith { error =>
                IO.println(s"Failed to map IPN: $error") *> IO.raiseError(error)
              }
              // TODO: Additional verification checks (recipient email, amount, duplicate, etc.)
              _ <- repo.save(newIpn).handleErrorWith { error =>
                IO.println(s"Failed to save IPN: $error")
              }
            } yield ()
          case "INVALID" =>
            IO.println("IPN invalid")
          case other =>
            IO.println(s"Unexpected validation response: $other")
        }.start // Run validation asynchronously

      } yield response
  }

  // Function to validate IPN with PayPal, adding retry logic
  private def validateWithRetry(client: Client[IO], rawBody: String, maxRetries: Int, delay: FiniteDuration): IO[String] = {
    val validationPayload = s"cmd=_notify-validate&$rawBody"
    val paypalValidationUrl = Uri.unsafeFromString(
      sys.env.getOrElse("PAYPAL_VALIDATION_URL", "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr")
    )

    def attempt(attemptNumber: Int): IO[String] = {
      client.expect[String](
        Request[IO](
          method = Method.POST,
          uri = paypalValidationUrl
        ).withEntity(validationPayload)
          .withHeaders(
            Header.Raw(CIString("content-type"), "application/x-www-form-urlencoded"),
            Header.Raw(CIString("user-agent"), "Scala-IPN-Verification-Script")
          )
      ).attempt.flatMap {
        case Right(response) => IO.pure(response)
        case Left(error) if attemptNumber < maxRetries =>
          IO.println(s"Validation attempt $attemptNumber failed: $error. Retrying...") *> IO.sleep(delay) *> attempt(attemptNumber + 1)
        case Left(error) =>
          IO.println(s"Validation failed after $maxRetries attempts: $error") *> IO.raiseError(error)
      }
    }

    attempt(1)
  }