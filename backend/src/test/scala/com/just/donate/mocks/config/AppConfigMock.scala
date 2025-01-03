package com.just.donate.mocks.config

import com.just.donate.config.Config

class AppConfigMock(
  val frontendUrl: String = "",
  val mongoUri: String = "",
  val mailSmtpHost: String = "",
  val mailSmtpPort: Int = 0,
  val mailAddress: String = "",
  val mailPassword: String = "",
) extends Config