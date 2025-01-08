package com.just.donate

import cats.effect.*
import com.comcast.ip4s.*
import com.just.donate.api.DonationRoute.donationRoute
import com.just.donate.api.NotificationRoute.notificationRoute
import com.just.donate.api.OrganisationRoute.organisationApi
import com.just.donate.api.PaypalRoute.paypalRoute
import com.just.donate.api.TransferRoute.transferRoute
import com.just.donate.api.WithdrawalRoute.withdrawalRoute
import com.just.donate.config.{AppConfig, AppEnvironment, Config}
import com.just.donate.db.PaypalRepository
import com.just.donate.models.{Donation, Donor, Organisation}
import com.just.donate.notify.{DevEmailService, EmailService, IEmailService}
import com.just.donate.store.FileStore
import com.just.donate.utils.Money
import org.http4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.mongodb.scala.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import cats.effect.unsafe.implicits.global

object Server extends IOApp:

/*
  var org = Organisation("Just-Donate")
  org = org.addAccount("Paypal")
  org = org.addAccount("Stripe")
  org = org.addAccount("Bank")

  org = org.addEarmarking("Education")
  org = org.addEarmarking("Health")


  val donor1 = Donor("1", "John", "john@example.org")
  val donor2 = Donor("2", "Jane", "jane@example.org")

  var donation = Donation(donor1.id, Money("100.00"))
  org = org.donate(donor1, donation._2, donation._1, "Paypal").toOption.get

  donation = Donation(donor2.id, Money("150.00"))
  org = org.donate(donor2, donation._2, donation._1, "Bank").toOption.get

  donation = Donation(donor1.id, Money("200.00"), "Education")
  org = org.donate(donor1, donation._2, donation._1, "Stripe").toOption.get

  donation = Donation(donor2.id, Money("250.00"), "Health")
  org = org.donate(donor2, donation._2, donation._1, "Paypal").toOption.get

  FileStore.save(org.name.hashCode.toString, org).unsafeRunSync()
*/
  private val appConfig: Config = AppConfig()
  private implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def run(args: List[String]): IO[ExitCode] =
    mongoResource(appConfig.mongoUri).use { client =>
      val database = client.getDatabase("just-donate")
      val paypalRepository = new PaypalRepository(database)

      FileStore.init()

      val emailService: IEmailService = appConfig.environment match
        case AppEnvironment.DEVELOPMENT => new DevEmailService(appConfig)
        case AppEnvironment.PRODUCTION  => new EmailService(appConfig)

      val httpApp: HttpApp[IO] = Router(
        "organisation" -> organisationApi(FileStore),
        "donate" -> donationRoute(FileStore, appConfig, emailService),
        "transfer" -> transferRoute(FileStore, appConfig, emailService),
        "withdraw" -> withdrawalRoute(FileStore, appConfig, emailService),
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
