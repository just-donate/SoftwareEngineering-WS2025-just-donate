package com.just.donate.utils.structs

import com.just.donate.models.{Donation, DonationPart}
import com.just.donate.utils.Money
import munit.FunSuite

class ReservableQueueSuite extends FunSuite:

  test("add and reserve the exact amount") {
    var queue = ReservableQueue("MainAccount")
    queue = queue.add(Donation("Donor", Money("100"))._2)

    val (amountRemaining, updatedQueue) = queue.reserve(Money("100"), "ContextA")

    // amountRemaining should be(None)
    assertEquals(amountRemaining, None)

    // Check that the first item is reserved by ContextA
    val reservable = updatedQueue.queue.head
    assert(reservable.isReserved, "Item should be reserved")
    assert(reservable.isReservedBy("ContextA"), "Item should be reserved by ContextA")
    assertEquals(reservable.value.amount, Money("100"))
  }

  test("reserve less than the available amount") {
    var queue = ReservableQueue("MainAccount")
    queue = queue.add(Donation("Donor", Money("100"))._2)

    val (amountRemaining, updatedQueue) = queue.reserve(Money("40"), "ContextA")

    // amountRemaining should be(None)
    assertEquals(amountRemaining, None)

    // Check that the first item is reserved by ContextA with amount 40
    val reservable1 = updatedQueue.queue.head
    assert(reservable1.isReserved, "First item should be reserved")
    assert(reservable1.isReservedBy("ContextA"), "First item should be reserved by ContextA")
    assertEquals(reservable1.value.amount, Money("40"))

    // Check that the second item is unreserved with amount 60
    val reservable2 = updatedQueue.queue(1)
    assert(!reservable2.isReserved, "Second item should be unreserved")
    assertEquals(reservable2.value.amount, Money("60"))
  }

  test("reserve more than the available amount") {
    var queue = ReservableQueue("MainAccount")
    queue = queue.add(Donation("Donor", Money("50"))._2)

    val (amountRemaining, updatedQueue) = queue.reserve(Money("100"), "ContextA")

    // amountRemaining shouldBe defined
    assert(amountRemaining.isDefined, "Amount remaining should be defined")
    assertEquals(amountRemaining.get, Money("50"))

    // Check that the item is reserved by ContextA with amount 50
    val reservable = updatedQueue.queue.head
    assert(reservable.isReserved, "Item should be reserved")
    assert(reservable.isReservedBy("ContextA"), "Item should be reserved by ContextA")
    assertEquals(reservable.value.amount, Money("50"))
  }

  test("handle multiple reserves from different contexts") {
    var queue = ReservableQueue("MainAccount")
    queue = queue.add(Donation("Donor", Money("100"))._2)
    queue = queue.add(Donation("Donor", Money("50"))._2)

    val (amountRemaining1, updatedQueue1) = queue.reserve(Money("80"), "ContextA")
    assertEquals(amountRemaining1, None)

    val (amountRemaining2, updatedQueue2) = updatedQueue1.reserve(Money("50"), "ContextB")
    assertEquals(amountRemaining2, None)

    // Check reservations
    val reservable1 = updatedQueue2.queue.head
    assert(reservable1.isReservedBy("ContextA"), "First part should be reserved by ContextA")
    assertEquals(reservable1.value.amount, Money("80"))

    val reservable2 = updatedQueue2.queue(1)
    assert(reservable2.isReservedBy("ContextB"), "Remainder of first item should be reserved by ContextB")
    assertEquals(reservable2.value.amount, Money("20"))

    val reservable3 = updatedQueue2.queue(2)
    assert(reservable3.isReservedBy("ContextB"), "Second donation part should be reserved by ContextB")
    assertEquals(reservable3.value.amount, Money("30"))

    val reservable4 = updatedQueue2.queue(3)
    assert(!reservable4.isReserved, "Remaining item should be unreserved")
    assertEquals(reservable4.value.amount, Money("20"))
  }

  test("not allow reserving an already reserved item by a different context") {
    var queue = ReservableQueue("MainAccount")
    queue = queue.add(Donation("Donor", Money("100"))._2)

    val (_, updatedQueue1) = queue.reserve(Money("100"), "ContextA")

    val (amountRemaining, updatedQueue2) = updatedQueue1.reserve(Money("50"), "ContextB")

    // amountRemaining shouldBe defined
    assert(amountRemaining.isDefined, "Should not be able to reserve from another context")
    assertEquals(amountRemaining.get, Money("50"))

    // Ensure that the item is still reserved by ContextA
    val reservable = updatedQueue2.queue.head
    assert(reservable.isReservedBy("ContextA"), "Item should remain reserved by ContextA")
    assert(reservable.isReserved, "Item should remain reserved")
  }

  test("reserve by splitting across multiple items") {
    var queue = ReservableQueue("MainAccount")
    queue = queue.add(Donation("Donor", Money("60"))._2)
    queue = queue.add(Donation("Donor", Money("40"))._2)

    val (amountRemaining, updatedQueue) = queue.reserve(Money("80"), "ContextA")

    // amountRemaining should be(None)
    assertEquals(amountRemaining, None)

    // Check that 60 from the first item and 20 from the second item are reserved
    val reservable1 = updatedQueue.queue.head
    assert(reservable1.isReservedBy("ContextA"), "60 should be reserved by ContextA")
    assertEquals(reservable1.value.amount, Money("60"))

    val reservable2 = updatedQueue.queue(1)
    assert(reservable2.isReservedBy("ContextA"), "20 should be reserved by ContextA")
    assertEquals(reservable2.value.amount, Money("20"))

    // Remaining unreserved amount from the second item
    val reservable3 = updatedQueue.queue(2)
    assert(!reservable3.isReserved, "Remaining amount should not be reserved")
    assertEquals(reservable3.value.amount, Money("20"))
  }

  test("handle insufficient resources gracefully") {
    var queue = ReservableQueue("MainAccount")
    queue = queue.add(Donation("Donor", Money("30"))._2)
    queue = queue.add(Donation("Donor", Money("20"))._2)

    val (amountRemaining, updatedQueue) = queue.reserve(Money("100"), "ContextA")

    // amountRemaining shouldBe defined
    assert(amountRemaining.isDefined, "Should have some unfulfilled amount")
    assertEquals(amountRemaining.get, Money("50"))

    // Check that all available resources are reserved
    updatedQueue.queue.foreach { reservable =>
      assert(reservable.isReservedBy("ContextA"), "All items should be reserved by ContextA")
    }
  }

  test("not reserve when no resources are available") {
    val queue = ReservableQueue("MainAccount")

    val (amountRemaining, updatedQueue) = queue.reserve(Money("50"), "ContextA")

    // amountRemaining shouldBe defined
    assert(amountRemaining.isDefined, "No resources available, so remainder is the full request")
    assertEquals(amountRemaining.get, Money("50"))

    // updatedQueue.queue shouldBe empty
    assert(updatedQueue.queue.isEmpty, "Queue should remain empty")
  }

  test("allow adding resources after attempting to reserve") {
    var queue = ReservableQueue("MainAccount")

    // Attempt to reserve before adding any resources
    val (_, updatedQueue1) = queue.reserve(Money("50"), "ContextA")

    // Adding resources after reservation attempt
    queue = updatedQueue1.add(Donation("Donor", Money("100"))._2)

    val (amountRemaining, updatedQueue2) = queue.reserve(Money("50"), "ContextA")

    // amountRemaining should be(None)
    assertEquals(amountRemaining, None)

    val reservable = updatedQueue2.queue.head
    assert(reservable.isReservedBy("ContextA"), "Newly added resource should now be reserved by ContextA")
    assertEquals(reservable.value.amount, Money("50"))
  }
