package com.just.donate

import cats.effect.*
import com.comcast.ip4s.*
import com.just.donate.api.DonationRoute.donationRoute
import com.just.donate.api.OrganisationRoute.organisationApi
import com.just.donate.api.PaypalRoute.paypalRoute
import com.just.donate.api.TransferRoute.transferRoute
import com.just.donate.api.WithdrawalRoute.withdrawalRoute
import com.just.donate.api.NotificationRoute.notificationRoute
import com.just.donate.config.{AppConfig, Config}
import com.just.donate.db.PaypalRepository
import com.just.donate.notify.{EmailService, IEmailService}
import com.just.donate.store.FileStore
import org.http4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.mongodb.scala.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Server extends IOApp:

  private val appConfig: Config = new AppConfig()
  private implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def run(args: List[String]): IO[ExitCode] =
    mongoResource(appConfig.mongoUri).use { client =>
      val database = client.getDatabase("just-donate")
      val paypalRepository = new PaypalRepository(database)

      FileStore.init()

      val emailService: IEmailService = new EmailService(appConfig)

      val httpApp: HttpApp[IO] = Router(
        "organisation" -> organisationApi(FileStore),
        "withdraw" -> withdrawalRoute(FileStore),
        "donate" -> donationRoute(FileStore, appConfig, emailService),
        "transfer" -> transferRoute(FileStore, appConfig, emailService),
        "notify" -> notificationRoute(appConfig),
        "paypal-ipn" -> paypalRoute(paypalRepository)
      ).orNotFound

      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(httpApp)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    }

  /** Acquire and safely release the Mongo client (using Resource). */
  private def mongoResource(uri: String): Resource[IO, MongoClient] =
    Resource.make(IO(MongoClient(uri)))(client => IO(client.close()))