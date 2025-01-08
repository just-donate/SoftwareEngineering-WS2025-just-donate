package com.just.donate.api

import cats.effect.IO
import com.just.donate.db.PaypalCrudRepository
import com.just.donate.models.PaypalIPN
import org.http4s.dsl.io.*
import org.http4s.{HttpRoutes, UrlForm}

object PaypalRoute:

  def paypalRoute: PaypalCrudRepository => HttpRoutes[IO] = (repo: PaypalCrudRepository) =>
    HttpRoutes.of[IO]:
      case GET -> Root =>
        // Maybe list everything from DB, or just memory buffer:
        for
          allDb <- repo.findAll
          _ <- IO.println(s"IPNs in DB: $allDb")
          resp <- Ok(allDb.mkString("\n"))
        yield resp

      case req @ POST -> Root =>
        for
          formData <- req.as[UrlForm].map(_.values.toMap) // Map[String, Seq[String]]
          _ <- IO.println(s"Received IPN Payload: $formData")

          // Create a new PaypalIPN entity and insert into DB
          newIpn = PaypalIPN(payload = formData.toString())
          _ <- repo.save(newIpn)

          resp <- Ok("IPN Payload received and stored in MongoDB")
        yield resp
