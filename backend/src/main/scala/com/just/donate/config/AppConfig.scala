package com.just.donate.config

import com.typesafe.config.{ConfigFactory, Config as TypesafeConfig}

import scala.util.Properties

object AppConfig:
  def apply(): AppConfig =
    val environment = Properties.envOrElse("ENV", "prod") match
      case "dev" | "development" => AppEnvironment.DEVELOPMENT
      case "prod" | "production" => AppEnvironment.PRODUCTION
      case env                   => throw new RuntimeException(f"Unknown environment: ${env}")

    AppConfig(
      environment match
        case AppEnvironment.DEVELOPMENT => ConfigFactory.load("application.dev")
        case AppEnvironment.PRODUCTION  => ConfigFactory.load()
      ,
      environment
    )

case class AppConfig(private val conf: TypesafeConfig, val environment: AppEnvironment) extends Config:
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
    else sys.env.get("MONGO_URI")

  private def expected[A](getter: String => Option[A]) =
    (path: String) =>
      getter(path).getOrElse(
        throw new RuntimeException(
          f"Expected the config option ${path} to be set in the config file or as an environment variable."
        )
      )

  private def getOptionalInt(path: String): Option[Int] =
    if conf.hasPath(path)
    then Some(conf.getInt(path))
    else Properties.envOrNone(path).map(_.toInt)
