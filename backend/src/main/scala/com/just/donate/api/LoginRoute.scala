package com.just.donate.api

import cats.effect.IO
import com.just.donate.config.AppEnvironment.PRODUCTION
import com.just.donate.config.Config
import com.just.donate.db.Repository
import com.just.donate.models.user.User
import io.circe.generic.auto.*
import org.http4s.SameSite.Strict
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.*
import pdi.jwt.{Jwt, JwtAlgorithm}

import java.security.spec.InvalidKeySpecException
import java.security.{NoSuchAlgorithmException, SecureRandom}
import java.time.Instant
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object LoginRoute:

  private val secretKey = sys.env.getOrElse("JWT_SECRET_KEY", "HAu/gwjy5124uMaX9wTAEPPXYDwsCYIWeZ7JnpRTRRk=")
  private val algorithm = JwtAlgorithm.HS256

  private val SALT_LENGTH = 16 // Length of the salt in bytes

  private val HASH_LENGTH = 256 // Length of the hash in bits

  private val ITERATIONS = 65536 // Number of iterations

  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  def hashPassword(password: String): String =
    // Generate a random salt
    val salt = generateSalt
    // Generate the hash
    val hash = generateHash(password, salt)
    // Combine salt and hash, then encode as a Base64 string
    Base64.getEncoder.encodeToString(salt) + ":" + Base64.getEncoder.encodeToString(hash)

  private def generateSalt =
    val random = new SecureRandom()
    val salt = new Array[Byte](SALT_LENGTH)
    random.nextBytes(salt)
    salt

  def loginRoute: (Config, Repository[String, User]) => HttpRoutes[IO] =
    (appConfig: Config, userRepo: Repository[String, User]) =>
      HttpRoutes.of[IO]:

        case req @ POST -> Root =>
          (for
            login <- req.as[LoginRequest]
            response <- authenticate(login, appConfig, userRepo)
          yield response).handleErrorWith { _ =>
            Unauthorized(`WWW-Authenticate`(Challenge("Basic", "Malformed request body")))
          }

  private def authenticate(
    login: LoginRequest,
    appConfig: Config,
    userRepo: Repository[String, User]
  ): IO[Response[IO]] =
    userRepo.findById(login.username).flatMap {
      case Some(user) =>
        // Verify that the user is active and the password matches
        if user.active && verifyPassword(login.password, user.password) then
          // Authentication successful
          val expirationTimeInSeconds = 3600 // 1 hour in seconds
          val currentTime = Instant.now().getEpochSecond
          val expirationTime = currentTime + expirationTimeInSeconds

          val httpDate = HttpDate
            .fromEpochSecond(expirationTime)
            .getOrElse(
              throw new RuntimeException("Invalid expiration time")
            )

          val claim =
            s"""{
                "username": "${login.username}",
                "orgId": "${login.orgId}",
                "exp": $expirationTime
              }"""

          val token = Jwt.encode(claim, secretKey, algorithm)
          val jwtCookie = ResponseCookie(
            name = "jwtToken",
            content = token,
            httpOnly = true,
            secure = appConfig.environment == PRODUCTION,
            path = Some("/"),
            sameSite = Some(Strict),
            maxAge = Some(expirationTimeInSeconds),
            expires = Some(httpDate)
          )
          Ok(LoginResponse("Login successful")).map(_.addCookie(jwtCookie))
        else Forbidden("Invalid credentials")

      case None =>
        // User not found; respond with forbidden
        Forbidden("Invalid credentials")
    }

  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  private def verifyPassword(password: String, stored: String): Boolean =
    // Split the stored value into salt and hash
    val parts = stored.split(":")
    val salt = Base64.getDecoder.decode(parts(0))
    val hash = Base64.getDecoder.decode(parts(1))
    // Generate a new hash with the provided password and the stored salt
    val newHash = generateHash(password, salt)
    // Compare the stored hash with the new hash
    java.util.Arrays.equals(hash, newHash)

  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  private def generateHash(password: String, salt: Array[Byte]) =
    val spec = new PBEKeySpec(password.toCharArray, salt, ITERATIONS, HASH_LENGTH);
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    factory.generateSecret(spec).getEncoded

  private case class LoginRequest(username: String, password: String, orgId: String)

  private case class LoginResponse(message: String)
