package com.just.donate.utils

import scala.math.{Numeric, ScalaNumber, ScalaNumericConversions}

// Define the Money class with a private constructor
case class Money private (private val amount: String):

  private[utils] def getAmount: String = amount

  // Addition operation
  def +(that: Money): Money =
    Money((BigDecimal(this.amount) + BigDecimal(that.amount)).toString())

  // Subtraction operation
  def -(that: Money): Money =
    Money((BigDecimal(this.amount) - BigDecimal(that.amount)).toString())

  // Multiplication operation
  def *(that: Money): Money =
    Money((BigDecimal(this.amount) * BigDecimal(that.amount)).toString())

  // Division operation
  def /(that: Money): Money =
    if that.amount == "0" then throw new ArithmeticException("Division by zero")
    else Money((BigDecimal(this.amount) / BigDecimal(that.amount)).toString())

  // Override toString to display the amount
  override def toString: String = amount

object Money:

  // Factory method to create Money instances from a String
  def apply(amount: String): Money = new Money(moneyStringTransform(amount))

  // Unapply method to enable pattern matching
  def unapply(money: Money): Option[String] = Some(money.amount)

  // Define a ZERO constant
  val ZERO: Money = Money("0")

  given Numeric[Money] with

    def plus(x: Money, y: Money): Money = x + y

    def minus(x: Money, y: Money): Money = x - y

    def times(x: Money, y: Money): Money = x * y

    def negate(x: Money): Money =
      Money((-BigDecimal(x.amount)).toString())

    def fromInt(x: Int): Money = Money(x.toString)

    def toInt(x: Money): Int = BigDecimal(x.amount).toInt

    def toLong(x: Money): Long = BigDecimal(x.amount).toLong

    def toFloat(x: Money): Float = BigDecimal(x.amount).toFloat

    def toDouble(x: Money): Double = BigDecimal(x.amount).toDouble

    def compare(x: Money, y: Money): Int = BigDecimal(x.amount).compare(BigDecimal(y.amount))

    def parseString(str: String): Option[Money] =
      try Some(Money(str))
      catch case _: NumberFormatException => None

  def moneyStringTransform(input: String): String =
    // Step 1: Trim whitespace
    val trimmed = input.trim

    // Step 2: Check for minus sign
    val negative = trimmed.startsWith("-")
    val sign = if (negative) "-" else ""

    // Step 3: Remove sign from the front if present
    val noSignStr = if (negative) trimmed.drop(1) else trimmed

    // Step 4: Split into integer and fractional parts
    //         If there's no decimal point, fractionPart remains empty.
    val parts = noSignStr.split("\\.", 2)
    val integerPart = if (parts.nonEmpty) parts(0) else ""
    val fractionPart = if (parts.length == 2) parts(1) else ""

    // Step 5: Strip leading zeros from integer part
    val strippedInt = integerPart.replaceFirst("^0+", "")
    val finalInt = if (strippedInt.isEmpty) "0" else strippedInt

    // Step 6: Strip trailing zeros from fraction part
    val strippedFrac = fractionPart.replaceAll("0+$", "")
    val finalFrac = if (strippedFrac.isEmpty) "0" else strippedFrac

    // Step 7: Reassemble with sign
    s"$sign$finalInt.$finalFrac"
