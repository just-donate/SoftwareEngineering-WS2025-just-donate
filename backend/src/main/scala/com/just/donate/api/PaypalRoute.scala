package com.just.donate.api

import cats.effect.IO
import com.just.donate.db.mongo.MongoPaypalRepository
import com.just.donate.models.PaypalIPN
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.{HttpRoutes, Method, Request, Uri}

object PaypalRoute:

  def paypalRoute(repo: MongoPaypalRepository, client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      for {
        allDb <- repo.findAll()
        _ <- IO.println(s"IPNs in DB: $allDb")
        resp <- Ok(allDb.mkString("\n"))
      } yield resp

    case req@POST -> Root =>
      for {
        // Read the raw request body
        rawBody <- req.bodyText.compile.string
        _ <- IO.println(s"Received raw IPN payload: $rawBody")

        // Immediately respond to PayPal with 200 OK
        response <- Ok("")

        // Perform asynchronous validation
        _ <- validateWithPaypal(client, rawBody).flatMap {
          case "VERIFIED" =>
            for {
              _ <- IO.println("IPN verified by PayPal")
              newIpn = PaypalIPN(payload = rawBody)
              _ <- repo.save(newIpn)
            } yield ()
          case "INVALID" =>
            IO.println("IPN invalid")
          case _ =>
            IO.println("Unexpected response from PayPal")
        }.start // Run validation asynchronously
      } yield response
  }

  private def validateWithPaypal(client: Client[IO], rawBody: String): IO[String] = {
    // Prepend cmd=_notify-validate to the raw body
    val validationPayload = s"cmd=_notify-validate&$rawBody"

    // Define PayPal's validation endpoint
    val paypalValidationUrl = Uri.unsafeFromString(sys.env.getOrElse("PAYPAL_VALIDATION_URL", "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr"))

    // Send the HTTPS POST to PayPal
    client.expect[String](Request[IO](
      method = Method.POST,
      uri = paypalValidationUrl
    ).withEntity(validationPayload))
  }