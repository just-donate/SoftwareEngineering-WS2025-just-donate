package com.just.donate.utils

import java.security.spec.InvalidKeySpecException
import java.security.{NoSuchAlgorithmException, SecureRandom}
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object CryptoUtils:
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

  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  def verifyPassword(password: String, stored: String): Boolean =
    // Split the stored value into salt and hash
    val parts = stored.split(":")
    val salt = Base64.getDecoder.decode(parts(0))
    val hash = Base64.getDecoder.decode(parts(1))
    // Generate a new hash with the provided password and the stored salt
    val newHash = generateHash(password, salt)
    // Compare the stored hash with the new hash
    java.util.Arrays.equals(hash, newHash)

  private def generateSalt =
    val random = new SecureRandom()
    val salt = new Array[Byte](SALT_LENGTH)
    random.nextBytes(salt)
    salt

  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  private def generateHash(password: String, salt: Array[Byte]) =
    val spec = new PBEKeySpec(password.toCharArray, salt, ITERATIONS, HASH_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    factory.generateSecret(spec).getEncoded 