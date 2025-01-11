package com.just.donate.mocks.config

import com.just.donate.config.{AppEnvironment, Config}

class AppConfigMock(
  val environment: AppEnvironment = AppEnvironment.DEVELOPMENT,
  val frontendUrl: String = "",
  val mongoUri: String = "",
  val mailSmtpHost: String = "",
  val mailSmtpPort: Int = 0,
  val mailAddress: String = "",
  val mailPassword: String = ""
) extends Config
