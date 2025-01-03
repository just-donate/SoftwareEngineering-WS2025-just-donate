package com.just.donate.config

import com.typesafe.config.{Config as TypesafeConfig, ConfigFactory}

import scala.util.Properties

class AppConfig(private val conf: TypesafeConfig) extends Config:
  def this() = {
    this(
      Properties.envOrElse("ENV", "prod") match
        case "dev" | "development" => ConfigFactory.load("application.dev")
        case "prod" | "production" => ConfigFactory.load()
        case env => throw new RuntimeException(f"Unknown environment: ${env}")
    )
  }

  val frontendUrl: String = getString("FRONTEND_URL")

  val mongoUri: String = getOptionalString("MONGO_URI").getOrElse("mongodb://localhost:27017")

  val mailSmtpHost: String = getString("MAIL_SMTP_HOST")
  val mailSmtpPort: Int = getOptionalInt("MAIL_SMTP_PORT").getOrElse(587)
  val mailAddress: String = getString("MAIL_ADDRESS")
  val mailPassword: String = getString("MAIL_PASSWORD")

  private def getString: String => String = expected(getOptionalString)

  private def getOptionalString(path: String): Option[String] =
    if conf.hasPath(path)
    then Some(conf.getString(path))
    else Properties.envOrNone(path)

  private def getOptionalInt(path: String): Option[Int] =
    if conf.hasPath(path)
    then Some(conf.getInt(path))
    else Properties.envOrNone(path).map(_.toInt)

  private def expected[A](getter: String => Option[A]) =
    (path: String) => getter(path).getOrElse(throw new RuntimeException(f"Expected the config option ${path} to be set in the config file or as an environment variable."))