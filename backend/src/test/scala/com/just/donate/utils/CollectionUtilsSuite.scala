package com.just.donate.utils

import com.just.donate.utils.CollectionUtils.*
import munit.FunSuite

class CollectionUtilsSuite extends FunSuite:

  test("updatedReturn should update element and return result when element exists") {
    val numbers = Seq(1, 2, 3, 4, 5)
    val (updatedSeq, result) = numbers.updatedReturn(_ == 3)(n => (n * 2, n + 10))
    
    assertEquals(updatedSeq, Seq(1, 2, 13, 4, 5))
    assertEquals(result, Some(6))
  }

  test("updatedReturn should return None when element doesn't exist") {
    val numbers = Seq(1, 2, 3, 4, 5)
    val (updatedSeq, result) = numbers.updatedReturn(_ == 10)(n => (n * 2, n + 10))
    
    assertEquals(updatedSeq, numbers)
    assertEquals(result, None)
  }

  test("updatedReturn should work with empty sequence") {
    val empty = Seq.empty[Int]
    val (updatedSeq, result) = empty.updatedReturn(_ => true)(n => (n * 2, n + 10))
    
    assertEquals(updatedSeq, empty)
    assertEquals(result, None)
  }

  test("updated should modify element when it exists") {
    val strings = Seq("apple", "banana", "cherry")
    val updated = strings.updated(_.startsWith("b"))("blackberry")
    
    assertEquals(updated, Seq("apple", "blackberry", "cherry"))
  }

  test("updated should not modify sequence when element doesn't exist") {
    val strings = Seq("apple", "banana", "cherry")
    val updated = strings.updated(_.startsWith("z"))("zebra")
    
    assertEquals(updated, strings)
  }

  test("updated should work with empty sequence") {
    val empty = Seq.empty[String]
    val updated = empty.updated(_ => true)("test")
    
    assertEquals(updated, empty)
  }

  test("tuple map1 should transform first element") {
    val tuple = ("hello", 42)
    val mapped = tuple.map1(_.length)
    
    assertEquals(mapped, (5, 42))
  }

  test("tuple map2 should transform second element") {
    val tuple = ("hello", 42)
    val mapped = tuple.map2(_ * 2)
    
    assertEquals(mapped, ("hello", 84))
  }

  test("tuple transformations should work with different types") {
    val tuple = (123, "world")
    val mapped1 = tuple.map1(_.toString)
    val mapped2 = tuple.map2(_.length)
    
    assertEquals(mapped1, ("123", "world"))
    assertEquals(mapped2, (123, 5))
  }

  test("complex scenario combining multiple operations") {
    val items = Seq((1, "one"), (2, "two"), (3, "three"))
    val (updatedItems, result) = items.updatedReturn(_._1 == 2)(
      tuple => (tuple.map2(_.toUpperCase), tuple.map1(_ * 10))
    )
    
    assertEquals(updatedItems, Seq((1, "one"), (20, "two"), (3, "three")))
    assertEquals(result, Some((2, "TWO")))
  } 