package com.just.donate.config

trait Config:
  val frontendUrl: String
  val mongoUri: String
  val mailSmtpHost: String
  val mailSmtpPort: Int
  val mailAddress: String
  val mailPassword: String