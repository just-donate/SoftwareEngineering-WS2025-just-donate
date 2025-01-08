package com.just.donate.utils.structs

import com.just.donate.utils.Money
import com.just.donate.utils.Money.ZERO
import munit.FunSuite
import scala.math.Ordered.orderingToOrdered

class MoneySuite extends FunSuite {

  // Helper method to assert Money equality
  def assertMoneyEquals(expected: String, actual: Money): Unit = {
    assertEquals(actual.getAmount, expected)
  }

  // Test the apply method with various inputs
  test("Money.apply should trim spaces and leading zeros") {
    val m1 = Money("  0123.45 ")
    assertMoneyEquals("123.45", m1)

    val m2 = Money("0000")
    assertMoneyEquals("0.0", m2)

    val m3 = Money(" 000123.4500 ")
    assertMoneyEquals("123.45", m3)

    val m4 = Money(" .5")
    assertMoneyEquals("0.5", m4)

    val m5 = Money("0.00")
    assertMoneyEquals("0.0", m5)

    val m6 = Money("000.00100")
    assertMoneyEquals("0.001", m6)

    val m7 = Money("0.00100")
    assertMoneyEquals("0.001", m7)

    val m8 = Money(".001")
    assertMoneyEquals("0.001", m8)

    val m9 = Money("10000.00001")
    assertMoneyEquals("10000.00001", m9)

    val m10 = Money("  -0123.45 ")
    assertMoneyEquals("-123.45", m10)

    val m11 = Money("-0000")
    assertMoneyEquals("-0.0", m11)

    val m12 = Money(" -000123.4500 ")
    assertMoneyEquals("-123.45", m12)

    val m13 = Money(" -.5")
    assertMoneyEquals("-0.5", m13)

    val m14 = Money("-0.00")
    assertMoneyEquals("-0.0", m14)

    val m15 = Money("-000.00100")
    assertMoneyEquals("-0.001", m15)

    val m16 = Money("-0.00100")
    assertMoneyEquals("-0.001", m16)

    val m17 = Money("-.001")
    assertMoneyEquals("-0.001", m17)

    val m18 = Money("-10000.00001")
    assertMoneyEquals("-10000.00001", m18)

    // 1. Integer only, with trailing decimal point
    val m19 = Money("123.")
    assertMoneyEquals("123.0", m19)

    // 2. Lots of leading zeros for both integer and fraction
    val m20 = Money("00000200.00030000")
    assertMoneyEquals("200.0003", m20)

    // 3. Zero integer, multiple trailing zeros in fraction
    val m21 = Money("0.100000")
    assertMoneyEquals("0.1", m21)

    // 4. Negative with trailing decimal point
    val m22 = Money("-0023.")
    assertMoneyEquals("-23.0", m22)

    // 5. Negative with fraction only
    val m23 = Money("-.9999")
    assertMoneyEquals("-0.9999", m23)

    // 6. Leading whitespace, trailing whitespace, decimal point in the middle
    val m24 = Money("    0012.34000   ")
    assertMoneyEquals("12.34", m24)

    // 7. Zero integer, empty fraction
    val m25 = Money(".")
    assertMoneyEquals("0.0", m25)

    // 8. Negative zero integer, empty fraction
    val m26 = Money(" -.  ")
    assertMoneyEquals("-0.0", m26)

    // 9. Integer zero only
    val m27 = Money(" 000 ")
    assertMoneyEquals("0.0", m27)

    // 10. Large negative integer, fractional zeros
    val m28 = Money("-999999999.0000")
    assertMoneyEquals("-999999999.0", m28)

  }

  // Test addition
  test("Money.+ should add two Money instances correctly") {
    val m1 = Money("100.50")
    val m2 = Money("200.75")
    val sum = m1 + m2
    assertMoneyEquals("301.25", sum)

    val m3 = Money("0")
    val sumWithZero = m1 + m3
    assertMoneyEquals("100.5", sumWithZero)

    val m4 = Money("-50.25")
    val sumNegative = m1 + m4
    assertMoneyEquals("50.25", sumNegative)
  }

  // Test subtraction
  test("Money.- should subtract two Money instances correctly") {
    val m1 = Money("300.00")
    val m2 = Money("100.50")
    val difference = m1 - m2
    assertMoneyEquals("199.5", difference)

    val m3 = Money("0")
    val differenceWithZero = m1 - m3
    assertMoneyEquals("300.0", differenceWithZero)

    val m4 = Money("350.00")
    val negativeDifference = m1 - m4
    assertMoneyEquals("-50.0", negativeDifference)
  }

  // Test multiplication
  test("Money.* should multiply two Money instances correctly") {
    val m1 = Money("10.00")
    val m2 = Money("20.50")
    val product = m1 * m2
    assertMoneyEquals("205.0", product)

    val m3 = Money("0")
    val productWithZero = m1 * m3
    assertMoneyEquals("0.0", productWithZero)

    val m4 = Money("-5.00")
    val negativeProduct = m1 * m4
    assertMoneyEquals("-50.0", negativeProduct)
  }

  // Test division
  test("Money./ should divide two Money instances correctly") {
    val m1 = Money("100.00")
    val m2 = Money("20.00")
    val quotient = m1 / m2
    assertMoneyEquals("5.0", quotient)

    val m3 = Money("3")
    val quotientFraction = m1 / m3
    assertMoneyEquals("33.33333333333333333333333333333333", quotientFraction)

    val m4 = Money("-25.00")
    val negativeQuotient = m1 / m4
    assertMoneyEquals("-4.0", negativeQuotient)

    // Division by zero should throw an exception
    intercept[ArithmeticException] {
      m1 / ZERO
    }
  }

  // Test comparison
  test("Money comparisons should work correctly") {
    val m1 = Money("100.00")
    val m2 = Money("200.00")
    val m3 = Money("100.00")
    val m4 = Money("-50.00")

    assert(m1 < m2)
    assert(m2 > m1)
    assert(m1 == m3)
    assert(m4 < ZERO)
    assert(m4 < m1)
    assert(!(m1 < m3))
    assert(!(m1 > m3))
  }

  // Test Numeric trait integration
  test("Money should work with Numeric trait operations") {
    import Money._

    val numeric = implicitly[Numeric[Money]]

    val m1 = Money("50.00")
    val m2 = Money("25.00")

    // Plus
    val sum = numeric.plus(m1, m2)
    assertMoneyEquals("75.0", sum)

    // Minus
    val difference = numeric.minus(m1, m2)
    assertMoneyEquals("25.0", difference)

    // Times
    val product = numeric.times(m1, m2)
    assertMoneyEquals("1250.0", product)

    // Negate
    val negated = numeric.negate(m1)
    assertMoneyEquals("-50.0", negated)

    // FromInt
    val fromInt = numeric.fromInt(100)
    assertMoneyEquals("100.0", fromInt)

    // ToInt
    val toInt = numeric.toInt(m1)
    assertEquals(toInt, 50)

    // ToLong
    val toLong = numeric.toLong(m1)
    assertEquals(toLong, 50L)

    // ToFloat
    val toFloat = numeric.toFloat(m1)
    assertEquals(toFloat, 50.00f)

    // ToDouble
    val toDouble = numeric.toDouble(m1)
    assertEquals(toDouble, 50.00)
  }

  // Test pattern matching with unapply
  test("Money.unapply should allow pattern matching") {
    val m = Money("123.45")

    m match {
      case Money(amount) => assertEquals(amount, "123.45")
      case _ => fail("Pattern matching failed")
    }

    val mZero = Money("0")
    mZero match {
      case Money(amount) => assertEquals(amount, "0.0")
      case _ => fail("Pattern matching failed for zero")
    }
  }

  // Test ZERO constant
  test("Money.ZERO should be equal to Money(\"0\")") {
    assertEquals(ZERO, Money("0"))
    assertEquals(ZERO, Money("0000"))
    assertEquals(ZERO, Money("  0  "))
  }

  // Test immutability
  test("Money instances should be immutable") {
    val m1 = Money("100.00")
    val m2 = m1 + Money("50.00")
    val m3 = m1 - Money("30.00")

    // Original m1 should remain unchanged
    assertMoneyEquals("100.0", m1)
    assertMoneyEquals("150.0", m2)
    assertMoneyEquals("70.0", m3)
  }

  // Test invalid inputs
  test("Money.apply should handle invalid inputs gracefully") {

    // Empty string should default to zero
    val m1 = Money("")
    assertMoneyEquals("0.0", m1)

    // String with only spaces should default to zero
    val m2 = Money("   ")
    assertMoneyEquals("0.0", m2)
  }

  // Test large numbers
  test("Money should handle very large numbers") {
    val large1 = Money("12345678901234567890.1234567890")
    val large2 = Money("98765432109876543210.9876543210")
    val sum = large1 + large2
    assertMoneyEquals("111111111011111111101.11111111", sum)

    val product = large1 * Money("2")
    assertMoneyEquals("24691357802469135780.246913578", product)

    val difference = large2 - large1
    assertMoneyEquals("86419753208641975320.864197532", difference)
  }

  // Test negative numbers
  test("Money should handle negative numbers correctly") {
    val m1 = Money("-100.50")
    val m2 = Money("50.25")
    val sum = m1 + m2
    assertMoneyEquals("-50.25", sum)

    val difference = m1 - m2
    assertMoneyEquals("-150.75", difference)

    val product = m1 * m2
    assertMoneyEquals("-5050.125", product)

    val quotient = m1 / m2
    assertMoneyEquals("-2.0", quotient)
  }

  // Test zero operations
  test("Money operations involving ZERO should behave correctly") {
    val m1 = Money("0")
    val m2 = Money("100.00")

    // Addition
    val sum = m1 + m2
    assertMoneyEquals("100.0", sum)

    // Subtraction
    val difference = m2 - m1
    assertMoneyEquals("100.0", difference)

    // Multiplication
    val product = m1 * m2
    assertMoneyEquals("0.0", product)

    // Division
    intercept[ArithmeticException] {
      m2 / ZERO
    }

    val quotient = m2 / Money("25")
    assertMoneyEquals("4.0", quotient)
  }

  // Test toString method
  test("Money.toString should return the amount as String") {
    val m1 = Money("123.45")
    assertEquals(m1.toString, "123.45")

    val m2 = Money("0")
    assertEquals(m2.toString, "0.0")

    val m3 = Money("000.00100")
    assertEquals(m3.toString, "0.001")
  }

  // Test equality and hashCode
  test("Money equality and hashCode should work correctly") {
    val m1 = Money("100.00")
    val m2 = Money("100.00")
    val m3 = Money("100.000")
    val m4 = Money("99.99")

    assertEquals(m1, m2)
    assertEquals(m1.hashCode(), m2.hashCode())

    // Depending on the trimming logic, m1 and m3 might be equal or not
    // Based on the apply method in the earlier implementation, "100.00" vs "100.000" are different
    assertEquals(m1, m3)

    assertNotEquals(m1, m4)
    assertNotEquals(m1.hashCode(), m4.hashCode())
  }
}
