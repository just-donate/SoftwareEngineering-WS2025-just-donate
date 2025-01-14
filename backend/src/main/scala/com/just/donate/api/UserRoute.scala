package com.just.donate.api

import cats.effect.IO
import com.just.donate.db.Repository
import com.just.donate.models.Organisation
import com.just.donate.models.user.{Roles, User}
import com.just.donate.utils.CryptoUtils
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

object UserRoute:

  // Define the user API, given a repository instance for User.
  def userApi(userRepo: Repository[String, User], orgRepo: Repository[String, Organisation]): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      case DELETE -> Root / email =>
        userRepo.delete(email) >> Ok(s"User with email $email deleted")

      case GET -> Root / "list" =>
        for
          users <- userRepo.findAll()
          response <- Ok(users.asJson)
        yield response

      case req @ PUT -> Root / email =>
        for
          update <- req.as[UpdateUser]
          maybeUser <- userRepo.findById(email)
          response <- maybeUser match
            case Some(user) =>
              val newRole = update.role.getOrElse(user.role)
              val newUser = user.copy(role = newRole, active = update.active.getOrElse(user.active))
              userRepo.update(newUser) >> Ok(newUser.asJson)
            case None => NotFound(s"User with email $email not found")
        yield response

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
              val hashedPassword = CryptoUtils.hashPassword(registerReq.password)

              existingOrg match
                case Some(_: Organisation) =>
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
              val updatedUser = user.copy(password = CryptoUtils.hashPassword(changeReq.newPassword))
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

  private case class UpdateUser(
    role: Option[String], // e.g. "ADMIN", "USER", "GUEST"
    active: Option[Boolean]
  )

  // For user registration (e.g., POST /user/register)
  private final case class RegisterUser(email: String, password: String, role: String, orgId: String)

  // For changing the password (e.g., POST /user/change-password)
  private final case class ChangePassword(email: String, newPassword: String)

  // Response payload for user-related endpoints.
  private final case class ResponseUser(
    email: String,
    role: String,
    active: Boolean,
    orgId: String
  )
