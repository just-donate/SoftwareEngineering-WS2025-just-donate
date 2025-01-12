package com.just.donate.api

import cats.effect.*
import cats.effect.std.Dispatcher
import com.comcast.ip4s.{ip, port}
import com.just.donate.db.memory.MemoryOrganisationRepository
import com.just.donate.helper.OrganisationHelper.{NEW_ROOTS, createNewRoots, organisationId}
import com.just.donate.mocks.config.AppConfigMock
import com.just.donate.mocks.notify.EmailServiceMock
import com.just.donate.mocks.paypal.MongoPaypalRepositoryMock
import com.just.donate.utils.Money
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.typelevel.ci.CIStringSyntax
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.io.{ByteArrayOutputStream, PrintStream}
import scala.concurrent.duration.*

class PaypalApiSuite extends CatsEffectSuite:

  private implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  // Use Dispatcher to run IO effects from non-IO code if needed.
  val disp: Resource[IO, Dispatcher[IO]] = Dispatcher[IO]

  private val orgRepo = MemoryOrganisationRepository()

  // Start a dummy validation server that always returns "INVALID"
  val dummyValidationServer: Resource[IO, org.http4s.server.Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(ip"127.0.0.1")
      .withPort(port"5123") // let the OS pick a free port
      .withHttpApp(
        HttpRoutes
          .of[IO] {
            case req @ POST -> Root =>
              // For any request, immediately return "INVALID" in the body.
              Ok("INVALID")
            case req @ POST -> Root / "valid" =>
              Ok("VERIFIED")
          }
          .orNotFound
      )
      .build

  val mockRepo = new MongoPaypalRepositoryMock(null)

  // Create the route we want to test.
  // Note that PaypalRoute uses sys.env.getOrElse("PAYPAL_VALIDATION_URL", ...) to obtain the URL.
  // We override the system property "PAYPAL_VALIDATION_URL" by passing the dummy server URL.
  def createRoute(validationUrl: String): HttpRoutes[IO] =
    // In order to override the URL used by validateWithRetry,
    // we temporarily set the environment variable. Since sys.env is immutable,
    // one strategy is to start the dummy server on a known port and rely on it.
    // (Our implementation calls: sys.env.getOrElse("PAYPAL_VALIDATION_URL", "...")
    // so by setting an actual environment variable when starting the process, we can
    // make sure the dummy URL is used. For testing, we simulate this by setting a JVM property.)
    // Note: This is a hack because sys.env is read-only.
    // In a real test you might refactor to inject the validation URL.
    System.setProperty("PAYPAL_VALIDATION_URL", validationUrl)
    PaypalRoute.paypalRoute(mockRepo, orgRepo, AppConfigMock(), EmailServiceMock())

  override def beforeEach(context: BeforeEach): Unit =
    mockRepo.reset()
    val initRepo = for
      _ <- orgRepo.clear()
      newRoots = createNewRoots()
      _ <- orgRepo.save(newRoots)
    yield ()
    initRepo.unsafeRunSync()

  test("POST /paypal-ipn returns immediate Ok and prints 'IPN invalid' when validation returns INVALID") {
    dummyValidationServer.use { server =>
      // Use the base endpoint (i.e. POST /) which our dummy returns as "INVALID".
      val validationUrl = s"http://${server.address.host.toString}:${server.address.port.toString}/"
      val route = createRoute(validationUrl)

      // Our sample PayPal IPN request body.
      val requestBody =
        "mc_gross=9.99&protection_eligibility=Eligible&payer_id=LPLWNMTBWMFAY&payment_date=23%3A04%3A11+Dec+25%2C+2024+PST&payment_status=Completed&charset=windows-1252&first_name=John&mc_fee=0.99&notify_version=3.9&custom=New+Roots&payer_status=verified&business=seller%40paypalsandbox.com&quantity=1&verify_sign=ABCDEFG12345&payer_email=buyer%40example.com&txn_id=1234567890&payment_type=instant&last_name=Doe&receiver_email=seller%40paypalsandbox.com&payment_fee=&receiver_id=S8XGHLYDW9T3S&txn_type=web_accept&item_name=&mc_currency=USD&item_number=&residence_country=US&test_ipn=1&transaction_subject=Donation&ipn_track_id=7e74f"

      // Build the POST request with required headers.
      val request = Request[IO](method = Method.POST, uri = uri"/")
        .withEntity(requestBody)
        .putHeaders(
          Header.Raw(ci"Content-Type", "application/x-www-form-urlencoded"),
          Header.Raw(ci"User-Agent", "PayPal IPN ( https://www.paypal.com/ipn )")
        )

      // Prepare to capture console output.
      val captureOut = new ByteArrayOutputStream()
      val ps = new PrintStream(captureOut)
      val originalOut = System.out

      IO.delay(System.setOut(ps)) *>
        route.orNotFound.run(request).flatMap { response =>
          IO.sleep(2.seconds) *> // wait for the asynchronous fiber to complete
            IO.delay {
              System.setOut(originalOut)
              captureOut.toString("UTF-8")
            }.map { captured =>
              val allDb = mockRepo.findAll().map(_.toList)
              assertEquals(response.status, Status.Ok)
              assertEquals(allDb.unsafeRunSync().length, 0)
              val updatedOrg = orgRepo.findById(organisationId(NEW_ROOTS)).unsafeRunSync().get
              println(">>>> ORG: " + updatedOrg)
              assert(updatedOrg.totalBalance == Money("0"))
              assert(clue(captured).contains("IPN invalid"), "Expected log output 'IPN invalid'")
            }
        }
    }
  }

  test(
    "POST /paypal-ipn with /valid returns immediate Ok and prints 'IPN verified by PayPal' when validation returns VERIFIED"
  ) {
    dummyValidationServer.use { server =>
      // Use the /valid endpoint so that the dummy server returns "VERIFIED".
      val validationUrl = s"http://${server.address.host.toString}:${server.address.port.toString}/valid"
      val route = createRoute(validationUrl)


      val requestBody =
        "mc_gross=100.00&protection_eligibility=Eligible&payer_id=LPLWNMTBWMFAY&payment_date=23%3A04%3A11+Dec+25%2C+2024+PST&payment_status=Completed&charset=windows-1252&first_name=John&mc_fee=0.99&notify_version=3.9&custom=New+Roots&payer_status=verified&business=seller%40paypalsandbox.com&quantity=1&verify_sign=ABCDEFG12345&payer_email=buyer%40example.com&txn_id=1234567890&payment_type=instant&last_name=Doe&receiver_email=seller%40paypalsandbox.com&payment_fee=&receiver_id=S8XGHLYDW9T3S&txn_type=web_accept&item_name=&mc_currency=USD&item_number=&residence_country=US&test_ipn=1&transaction_subject=Donation&ipn_track_id=7e74f"

      val request = Request[IO](method = Method.POST, uri = uri"/")
        .withEntity(requestBody)
        .putHeaders(
          Header.Raw(ci"Content-Type", "application/x-www-form-urlencoded"),
          Header.Raw(ci"User-Agent", "PayPal IPN ( https://www.paypal.com/ipn )")
        )

      // Capture console output.
      val captureOut = new ByteArrayOutputStream()
      val ps = new PrintStream(captureOut)
      val originalOut = System.out

      IO.delay(System.setOut(ps)) *>
        route.orNotFound.run(request).flatMap { response =>
          IO.sleep(2.seconds) *> // allow time for asynchronous processing
            IO.delay {
              System.setOut(originalOut)
              captureOut.toString("UTF-8")
            }.map { captured =>
              val allDb = mockRepo.findAll().map(_.toList)
              assertEquals(response.status, Status.Ok)
              assertEquals(allDb.unsafeRunSync().length, 1)
              val updatedOrg = orgRepo.findById(organisationId(NEW_ROOTS)).unsafeRunSync().get
              println(">>>> ORG: " + updatedOrg)
              assert(updatedOrg.totalBalance == Money("100"))
              assert(clue(captured).contains("IPN verified by PayPal"), "Expected log output 'IPN verified by PayPal'")
            }
        }
    }
  }
