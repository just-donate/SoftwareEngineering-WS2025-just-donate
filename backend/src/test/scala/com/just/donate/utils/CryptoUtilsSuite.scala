package com.just.donate.utils

import com.just.donate.utils.CryptoUtils
import munit.FunSuite

class CryptoUtilsSuite extends FunSuite {

  test("hashPassword should generate a non-empty hash") {
    val password = "admin"
    val hashedPassword = CryptoUtils.hashPassword(password)
    assert(hashedPassword.nonEmpty, "Hashed password should not be empty")
  }

  test("verifyPassword should return true for correct password") {
    val password = "admin"
    val hashedPassword = CryptoUtils.hashPassword(password)
    val isValid = CryptoUtils.verifyPassword(password, hashedPassword)
    assert(isValid, "Password verification should be successful")
  }

  test("verifyPassword should return false for incorrect password") {
    val password = "admin"
    val hashedPassword = CryptoUtils.hashPassword(password)
    val wrongPassword = "wrong"
    val isInvalid = CryptoUtils.verifyPassword(wrongPassword, hashedPassword)
    assert(!isInvalid, "Password verification should fail for incorrect password")
  }
