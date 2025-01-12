package com.just.donate.api

import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.models.user.User
import de.mkammerer.argon2.Argon2Factory
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object UserRoute:

  private val argon2 = Argon2Factory.create()

  // Define the user API, given a repository instance for User.
  def userApi(userRepo: Repository[String, User], orgRepo: Repository[String, Organisation]): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      // Register a new user: POST /user/register
      case req @ POST -> Root / "register" =>
        for
          registerReq <- req.as[RegisterUser]
          // Check for duplicates by email
          existingUser <- userRepo.findById(registerReq.email)
          existingOrg <- orgRepo.findById(registerReq.orgId)
          response <- existingUser match
            case Some(_) =>
              // If a user with this email already exists, return a conflict error.
              Conflict(s"User with email ${registerReq.email} already exists.")
            case None =>
              // No duplicate found—proceed to hash the password and create the user.
              val hashedPassword = hashPassword(registerReq.password)

              existingOrg.getOrElse(None) match
                case com.just.donate.models.Organisation(_, _, _, _, _, _) =>
                  val newUser = User(email = registerReq.email, password = hashedPassword, orgId = registerReq.orgId)
                  for
                    _ <- userRepo.save(newUser)
                    response = ResponseUser(
                      email = newUser.email,
                      role = newUser.role.toString,
                      active = newUser.active,
                      orgId = newUser.orgId
                    )
                    r <- Ok(response.asJson)
                  yield r
                case None => NotFound(s"Organisation with id ${registerReq.orgId} not found.")
        yield response

      // Change the user's password: POST /user/change-password
      case req @ POST -> Root / "change-password" =>
        for
          changeReq <- req.as[ChangePassword]
          // Convert the provided string id to an ObjectId
          maybeUser <- userRepo.findById(changeReq.email)
          resp <- maybeUser match
            case Some(user) =>
              val updatedUser = user.copy(password = hashPassword(changeReq.newPassword))
              for
                _ <- userRepo.update(updatedUser)

                response = ResponseUser(
                  email = updatedUser.email,
                  role = updatedUser.role.toString,
                  active = updatedUser.active,
                  orgId = updatedUser.orgId
                )
                r <- Ok(response.asJson)
              yield r
            case None =>
              NotFound(s"User with email ${changeReq.email} not found.")
        yield resp
    }

  private def hashPassword(password: String): String =
    argon2.hash(3, 1 << 12, 1, password)

  // For user registration (e.g., POST /user/register)
  private final case class RegisterUser(email: String, password: String, orgId: String)

  // For changing the password (e.g., POST /user/change-password)
  private final case class ChangePassword(email: String, newPassword: String)

  // Response payload for user-related endpoints.
  private final case class ResponseUser(
    email: String,
    role: String,
    active: Boolean,
    orgId: String
  )
