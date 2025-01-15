package com.just.donate.api

import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.db.mongo.{MongoOrganisationRepository, MongoUserRepository}
import com.just.donate.models.Organisation
import com.just.donate.models.user.{Roles, User}
import io.circe.parser.decode
import io.circe.syntax.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.implicits.*
import org.mockito.ArgumentMatchers.*
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.*
import org.mockito.{Mockito, MockitoSugar}

class UserRouteSuite extends CatsEffectSuite with MockitoSugar:

  // --------------------------------------------------------
  // Mocks for userRepo and orgRepo
  // --------------------------------------------------------
  private val mockUserRepo = Mockito.mock(classOf[MongoUserRepository])
  private val mockOrgRepo = Mockito.mock(classOf[MongoOrganisationRepository])

  // The route under test. We typically wrap it in `.orNotFound` so we get an HttpApp.
  private val route: HttpApp[IO] = UserRoute.userApi(mockUserRepo, mockOrgRepo).orNotFound

  // Utility to reset mocks before each test if desired
  override def beforeEach(context: BeforeEach): Unit =
    reset(mockUserRepo, mockOrgRepo)

  // ----------------------------------------------------------------------
  // Test 1: DELETE /:email
  // ----------------------------------------------------------------------
  test("DELETE /:email should call userRepo.delete and return 200") {
    Mockito.doReturn(IO.unit).when(mockUserRepo).delete("test@example.com")

    val req = Request[IO](method = Method.DELETE, uri = uri"/test@example.com")

    for
      resp <- route.run(req)
      body <- resp.as[String]
    yield
      assertEquals(resp.status, Status.Ok)
      assert(body.contains("User with email test@example.com deleted"))
      verify(mockUserRepo, times(1)).delete("test@example.com")
  }

  // ----------------------------------------------------------------------
  // Test 2: GET /list
  // ----------------------------------------------------------------------
  test("GET /list should return list of users as JSON") {
    // Adjust these according to your user model's parameter order:
    val users = List(
      User(
        email = "user1@example.com",
        password = "hashedPassword1",
        role = Roles.USER.toString,
        active = true,
        orgId = "org1"
      ),
      User(
        email = "user2@example.com",
        password = "hashedPassword2",
        role = Roles.ADMIN.toString,
        active = false,
        orgId = "org2"
      )
    )

    Mockito.doReturn(IO.pure(users)).when(mockUserRepo).findAll()

    val req = Request[IO](method = Method.GET, uri = uri"/list")

    for
      resp <- route.run(req)
      body <- resp.bodyText.compile.string
    yield
      assertEquals(resp.status, Status.Ok)
      // We can decode the JSON string back into a list of users or just check substrings
      assert(body.contains("user1@example.com"))
      assert(body.contains("user2@example.com"))
  }

  // ----------------------------------------------------------------------
  // Test 3.1: PUT /:email (user found => update role, active)
  // ----------------------------------------------------------------------
  test("PUT /:email with existing user => updates user, returns 200") {
    val existingUser = User(
      email = "test@example.com",
      password = "hashedPass",
      role = Roles.USER.toString,
      active = true,
      orgId = "org1"
    )

    Mockito.doReturn(IO.pure(Some(existingUser))).when(mockUserRepo).findById("test@example.com")
    Mockito.doReturn(IO.unit).when(mockUserRepo).update(any[User])

    // JSON body with new role and active = false
    val updateJson =
      s"""
         |{
         |  "role": "ADMIN",
         |  "active": false
         |}
         |""".stripMargin

    val req = Request[IO](
      method = Method.PUT,
      uri = uri"/test@example.com"
    ).withEntity(updateJson)

    for
      resp <- route.run(req)
      body <- resp.bodyText.compile.string
    yield
      assertEquals(resp.status, Status.Ok)
      assert(body.contains("test@example.com"))
      assert(body.contains("ADMIN")) // updated role
      assert(body.contains("false")) // updated active
      // Verify userRepo calls
      verify(mockUserRepo, times(1)).findById("test@example.com")
      verify(mockUserRepo, times(1)).update(any[User])
  }

  // ----------------------------------------------------------------------
  // Test 3.2: PUT /:email with user not found => 404
  // ----------------------------------------------------------------------
  test("PUT /:email with non-existing user => 404 NotFound") {
    Mockito.doReturn(IO.pure(None)).when(mockUserRepo).findById("nope@example.com")

    val updateJson =
      """
        |{
        |  "role": "USER",
        |  "active": true
        |}
        |""".stripMargin

    val req = Request[IO](
      method = Method.PUT,
      uri = uri"/nope@example.com"
    ).withEntity(updateJson)

    for
      resp <- route.run(req)
      body <- resp.bodyText.compile.string
    yield
      assertEquals(resp.status, Status.NotFound)
      assert(body.contains("nope@example.com not found"))
  }

  // ----------------------------------------------------------------------
  // Test 4.1: POST /register => success
  // ----------------------------------------------------------------------
  test("POST /register with new user, existing org => 200 + new user JSON") {
    val existingUser = User(
      email = "exists@example.com",
      password = "someHash",
      role = Roles.USER.toString,
      active = true,
      orgId = "org-id"
    )
    // No user found with that email => None
    Mockito.doReturn(IO.pure(None)).when(mockUserRepo).findById(any())
    // Org found => Some(org)
    val dummyOrg = Organisation("org-id")
    Mockito.doReturn(IO.pure(Some(dummyOrg))).when(mockOrgRepo).findById(any())
    // Save user
    Mockito.doReturn(IO.pure(existingUser)).when(mockUserRepo).save(any())

    val registerJson =
      """
        |{
        |  "email": "new@example.com",
        |  "password": "PlainPassword",
        |  "role": "USER",
        |  "orgId": "org-id"
        |}
        |""".stripMargin

    val req = Request[IO](method = Method.POST, uri = uri"/register").withEntity(registerJson)

    for
      resp <- route.run(req)
      bodyStr <- resp.bodyText.compile.string
    yield
      assertEquals(resp.status, Status.Ok)
      assert(bodyStr.contains("new@example.com"))
      assert(bodyStr.contains("org-id"))
      // verify repos
      verify(mockUserRepo, times(1)).findById("new@example.com")
      verify(mockOrgRepo, times(1)).findById("org-id")
      verify(mockUserRepo, times(1)).save(any[User])
  }

  // ----------------------------------------------------------------------
  // Test 4.2: POST /register => user already exists => 409 Conflict
  // ----------------------------------------------------------------------
  test("POST /register with existing user => 409 Conflict") {
    val existingUser = User(
      email = "exists@example.com",
      password = "someHash",
      role = Roles.USER.toString,
      active = true,
      orgId = "org-id"
    )

    Mockito.doReturn(IO.pure(None)).when(mockUserRepo).findById("exists@example.com")

    val registerJson =
      """
        |{
        |  "email": "exists@example.com",
        |  "password": "PlainPassword",
        |  "role": "USER",
        |  "orgId": "org-id"
        |}
        |""".stripMargin

    val req = Request[IO](method = Method.POST, uri = uri"/register").withEntity(registerJson)
    val dummyOrg = Organisation("org-id")
    Mockito.doReturn(IO.pure(Some(dummyOrg))).when(mockOrgRepo).findById(any())
    Mockito.doReturn(IO.pure(Some(existingUser))).when(mockUserRepo).findById(any())

    for
      resp <- route.run(req)
      bodyStr <- resp.bodyText.compile.string
    yield
      assertEquals(resp.status, Status.Conflict)
      assert(bodyStr.contains("User with email exists@example.com already exists."))
  }

  // ----------------------------------------------------------------------
  // Test 4.3: POST /register => Org not found => 404
  // ----------------------------------------------------------------------
  test("POST /register => 404 if organisation not found") {
    // user not found => None
    Mockito.doReturn(IO.pure(None)).when(mockUserRepo).findById("newbie@example.com")

    // org not found => None
    Mockito.doReturn(IO.pure(None)).when(mockOrgRepo).findById(any())

    val registerJson =
      """
        |{
        |  "email": "newbie@example.com",
        |  "password": "secretPass",
        |  "role": "USER",
        |  "orgId": "bad-org"
        |}
        |""".stripMargin

    val req = Request[IO](method = Method.POST, uri = uri"/register").withEntity(registerJson)

    for
      resp <- route.run(req)
      bodyStr <- resp.bodyText.compile.string
    yield
      assertEquals(resp.status, Status.NotFound)
      assert(bodyStr.contains("Organisation with id bad-org not found."))
  }

  // ----------------------------------------------------------------------
  // Test 5.1: POST /change-password => success
  // ----------------------------------------------------------------------
  test("POST /change-password => user found => 200 and updated user JSON") {
    val existingUser = User(
      email = "change@example.com",
      password = "oldHash",
      role = Roles.USER.toString,
      active = true,
      orgId = "org-id"
    )

    Mockito.doReturn(IO.pure(Some(existingUser))).when(mockUserRepo).findById("change@example.com")
    // on update, do nothing
    Mockito.doReturn(IO.pure(existingUser)).when(mockUserRepo).update(any[User])

    val bodyJson =
      """
        |{
        |  "email": "change@example.com",
        |  "newPassword": "NewPlainPass"
        |}
        |""".stripMargin

    val req = Request[IO](method = Method.POST, uri = uri"/change-password").withEntity(bodyJson)

    for
      resp <- route.run(req)
      bodyStr <- resp.bodyText.compile.string
    yield
      assertEquals(resp.status, Status.Ok)
      assert(bodyStr.contains("change@example.com"))
      // Check calls
      verify(mockUserRepo, times(1)).findById("change@example.com")
      verify(mockUserRepo, times(1)).update(any[User])
  }

  // ----------------------------------------------------------------------
  // Test 5.2: POST /change-password => user not found => 404
  // ----------------------------------------------------------------------
  test("POST /change-password => 404 if user not found") {
    Mockito.doReturn(IO.pure(None)).
        when(mockUserRepo).findById("missing@example.com")

    val bodyJson =
      """
        |{
        |  "email": "missing@example.com",
        |  "newPassword": "NewPass"
        |}
        |""".stripMargin

    val req = Request[IO](method = Method.POST, uri = uri"/change-password").withEntity(bodyJson)

    for
      resp <- route.run(req)
      bodyStr <- resp.bodyText.compile.string
    yield
      assertEquals(resp.status, Status.NotFound)
      assert(bodyStr.contains("User with email missing@example.com not found."))
  }
