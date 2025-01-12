package com.just.donate.api

import cats.effect.IO
import com.just.donate.db.mongo.MongoPaypalRepository
import com.just.donate.models.PaypalIPN
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.dsl.io.*

object PaypalRoute:

  def paypalRoute(repo: MongoPaypalRepository, client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      for
        allDb <- repo.findAll()
        _ <- IO.println(s"IPNs in DB: $allDb")
        resp <- Ok(allDb.mkString("\n"))
      yield resp

    case req @ POST -> Root =>
      for
        // Read the raw request body
        rawBody <- req.bodyText.compile.string
        newIpn <- req.as[PaypalIPN]
        _ <- IO.println(s"Received raw IPN payload: $rawBody")

        // Immediately respond to PayPal with 200 OK
        response <- Ok("")

        // Perform asynchronous validation
        _ <- validateWithPaypal(client, rawBody).flatMap {
          case "VERIFIED" =>
            for _ <- IO.println("IPN verified by PayPal")

              // TODO: Verify that you are the intended recipient of the IPN message. To do this, check the email address in the message. This check prevents another merchant from accidentally or intentionally using your listener.

              // TODO: Verify that the IPN is not a duplicate. To do this, save the transaction ID and last payment status in each IPN message in a database and verify that the current IPN's values for these fields are not already in this database.

              // TODO: Ensure that you receive an IPN whose payment status is "completed" before shipping merchandise or enabling download of digital goods. Because IPN messages can be sent at various stages in a transaction's progress, you must wait for the IPN whose status is "completed' before handing over merchandise to a customer.

              // TODO: Verify that the payment amount in an IPN matches the price you intend to charge. If you do not encrypt your button code, it's possible for someone to capture a button-click message and change the price it contains. If you don't check the price in an IPN against the real price, you could accept a lower payment than you want.
              _ <- repo.save(newIpn)
            yield ()
          case "INVALID" =>
            IO.println("IPN invalid")
          case _ =>
            IO.println("Unexpected response from PayPal")
        }.start // Run validation asynchronously
      yield response
  }

  private def validateWithPaypal(client: Client[IO], rawBody: String): IO[String] =
    // Prepend cmd=_notify-validate to the raw body
    val validationPayload = s"cmd=_notify-validate&$rawBody"

    // Define PayPal's validation endpoint
    val paypalValidationUrl = Uri.unsafeFromString(
      sys.env.getOrElse("PAYPAL_VALIDATION_URL", "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr")
    )

    // Send the HTTPS POST to PayPal
    client.expect[String](
      Request[IO](
        method = Method.POST,
        uri = paypalValidationUrl
      ).withEntity(validationPayload)
    )
