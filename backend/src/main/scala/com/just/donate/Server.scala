package com.just.donate

import cats.effect.*
import com.comcast.ip4s.*
import com.just.donate.api.DonationRoute.donationRoute
import com.just.donate.api.NotificationRoute.notificationRoute
import com.just.donate.api.OrganisationRoute.organisationApi
import com.just.donate.api.PaypalRoute.paypalRoute
import com.just.donate.api.TransferRoute.transferRoute
import com.just.donate.api.WithdrawalRoute.withdrawalRoute
import com.just.donate.api.public.{DonationPublicRoute, OrganizationPublicRoute}
import com.just.donate.api.{CheckAuthRoute, LoginRoute, LogoutRoute, UserRoute}
import com.just.donate.config.{AppConfig, AppEnvironment, Config}
import com.just.donate.db.mongo.{MongoErrorLogRepository, MongoOrganisationRepository, MongoPaypalRepository, MongoUserRepository}
import com.just.donate.notify.{DevEmailService, EmailService, IEmailService}
import com.just.donate.security.AuthMiddleware
import com.just.donate.utils.ErrorLogger
import org.http4s.*
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.*
import org.http4s.headers.Origin
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.mongodb.scala.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import scala.concurrent.duration.DurationInt

object Server extends IOApp:

  private val appConfig: Config = AppConfig()

  private implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def run(args: List[String]): IO[ExitCode] =
    mongoResource(appConfig.mongoUri).flatMap { mongoClient =>
      httpClientResource.map { httpClient =>
        (mongoClient, httpClient)
      }
    }.use { client =>
      val database = client._1.getDatabase("just-donate")

      val organisationCollection = database.getCollection("organisations")
      val paypalCollection = database.getCollection("paypal_ipn")
      val userCollection = database.getCollection("users")
      val errorLogCollection = database.getCollection("error_logs")

      val organisationRepository = MongoOrganisationRepository(organisationCollection)
      val paypalRepository = MongoPaypalRepository(paypalCollection)
      val userRepository = MongoUserRepository(userCollection)
      val errorLogRepository = MongoErrorLogRepository(errorLogCollection)

      /**
       * Error logger to log errors to the database.
       * Can be passed in to other services that need to log errors and can showcase them in the UI.
       */
      val errorLogger = new ErrorLogger(errorLogRepository)

      //      val defaultOrg = Organisation("Just-Donate")
      //      val org = organisationRepository.findById(defaultOrg.id).unsafeRunSync()
      //      if org.isEmpty then organisationRepository.save(Organisation("Just-Donate")).unsafeRunSync()

      val emailService: IEmailService = appConfig.environment match
        case AppEnvironment.DEVELOPMENT => new DevEmailService(appConfig)
        case AppEnvironment.PRODUCTION => new EmailService(appConfig)

      // <editor-fold desc="Organization Routes">
      val securedOrganisationApi: HttpRoutes[IO] = AuthMiddleware.apply(organisationApi(organisationRepository))
      val publicOrganisationApi: HttpRoutes[IO] = OrganizationPublicRoute.publicApi(organisationRepository)
      // </editor-fold>

      // <editor-fold desc="Authentication Routes">
      val securedLogoutRoute: HttpRoutes[IO] = AuthMiddleware.apply(LogoutRoute.logoutRoute(appConfig))
      val securedCheckAuthRoute: HttpRoutes[IO] = AuthMiddleware.apply(CheckAuthRoute.checkAuthRoute)
      val securedRegisterRoute: HttpRoutes[IO] =
        AuthMiddleware.apply(UserRoute.userApi(userRepository, organisationRepository))
      // </editor-fold>

      // <editor-fold desc="Donation, Transfer, Withdrawal, and Notification Routes">
      val publicDonationRoute: HttpRoutes[IO] = DonationPublicRoute.donationRoute(organisationRepository, appConfig, emailService)
      val securedDonationRoute: HttpRoutes[IO] =
        AuthMiddleware.apply(donationRoute(organisationRepository, appConfig, emailService))
      val securedTransferRoute: HttpRoutes[IO] =
        AuthMiddleware.apply(transferRoute(organisationRepository, appConfig, emailService))
      val securedWithdrawalRoute: HttpRoutes[IO] =
        AuthMiddleware.apply(withdrawalRoute(organisationRepository, appConfig, emailService))
      val securedNotificationRoute: HttpRoutes[IO] = AuthMiddleware.apply(notificationRoute(appConfig))
      // </editor-fold>

      val httpApp: HttpApp[IO] = Router(
        "login" -> LoginRoute.loginRoute(appConfig, userRepository),
        "user" -> securedRegisterRoute,
        "check-auth" -> securedCheckAuthRoute,
        "logout" -> securedLogoutRoute,
        "organisation" -> securedOrganisationApi,
        "public/organisation" -> publicOrganisationApi,
        "donate" -> securedDonationRoute,
        "public/donate" -> publicDonationRoute,
        "transfer" -> securedTransferRoute,
        "withdraw" -> securedWithdrawalRoute,
        "notify" -> securedNotificationRoute,
        "paypal-ipn" -> paypalRoute(paypalRepository, organisationRepository, appConfig, emailService, errorLogger)
      ).orNotFound

      val corsService = CORS.policy
        .withAllowOriginHost(
          Set(
            Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), Some(3000)),
            Origin.Host(Uri.Scheme.https, Uri.RegName("paypal.com"), None),
            Origin.Host(Uri.Scheme.https, Uri.RegName("sandbox.paypal.com"), None),
            Origin.Host(Uri.Scheme.https, Uri.RegName("ipnpb.sandbox.paypal.com"), None),
            Origin.Host(Uri.Scheme.https, Uri.RegName("ipnpb.paypal.com"), None),
            Origin.Host(Uri.Scheme.https, Uri.RegName("just-donate.github.io"), None)
          )
        )
        .withAllowMethodsIn(Set(Method.GET, Method.POST, Method.DELETE, Method.PUT))
        .withAllowCredentials(true)
        .withMaxAge(1.days)
        .apply(httpApp)

      for
        service <- corsService
        server <- EmberServerBuilder
          .default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(service)
          .build
          .use(_ => IO.never)
          .as(ExitCode.Success)
      yield server
    }

  /** Acquire and safely release the Mongo client (using Resource). */
  private def mongoResource(uri: String): Resource[IO, MongoClient] =
    Resource.make(IO(MongoClient(uri)))(client => IO(client.close()))

  /** Acquire and safely release the HTTP client (using Resource). */
  private def httpClientResource: Resource[IO, Client[IO]] =
    EmberClientBuilder.default[IO].build
