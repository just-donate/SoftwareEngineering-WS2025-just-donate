package com.just.donate.utils.structs

import com.just.donate.models.{Donation, DonationPart}
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.*

class ReservableQueueTest extends AnyFlatSpec with should.Matchers:

  "A ReservableQueue" should "add and reserve the exact amount" in {
    var queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(100)))

    val (amountRemaining, updatedQueue) = queue.reserve(BigDecimal.valueOf(100), "ContextA")

    amountRemaining should be(None)

    // Check that the first item is reserved by ContextA
    val reservable = updatedQueue.queue.head

    reservable.isReserved should be(true)
    reservable.isReservedBy("ContextA") should be(true)
    reservable.value.amount should be(BigDecimal.valueOf(100))
  }

  it should "reserve less than the available amount" in {
    var queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(100)))

    val (amountRemaining, updatedQueue) = queue.reserve(BigDecimal.valueOf(40), "ContextA")

    amountRemaining should be(None)

    // Check that the first item is reserved by ContextA with amount 40
    val reservable1 = updatedQueue.queue.head
    reservable1.isReserved should be(true)
    reservable1.isReservedBy("ContextA") should be(true)
    reservable1.value.amount should be(BigDecimal.valueOf(40))

    // Check that the second item is unreserved with amount 60
    val reservable2 = updatedQueue.queue(1)
    reservable2.isReserved should be(false)
    reservable2.value.amount should be(BigDecimal.valueOf(60))
  }

  it should "reserve more than the available amount" in {
    var queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(50)))

    val (amountRemaining, updatedQueue) = queue.reserve(BigDecimal.valueOf(100), "ContextA")

    amountRemaining shouldBe defined
    amountRemaining.get should be(BigDecimal.valueOf(50))

    // Check that the item is reserved by ContextA with amount 50
    val reservable = updatedQueue.queue.head
    reservable.isReserved should be(true)
    reservable.isReservedBy("ContextA") should be(true)
    reservable.value.amount should be(BigDecimal.valueOf(50))
  }

  it should "handle multiple reserves from different contexts" in {
    var queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(100)))
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(50)))

    val (amountRemaining1, updatedQueue1) = queue.reserve(BigDecimal.valueOf(80), "ContextA")
    amountRemaining1 should be(None)

    val (amountRemaining2, updatedQueue2) = updatedQueue1.reserve(BigDecimal.valueOf(50), "ContextB")
    amountRemaining2 should be(None)

    // Check reservations
    val reservable1 = updatedQueue2.queue.head
    reservable1.isReservedBy("ContextA") should be(true)
    reservable1.value.amount should be(BigDecimal.valueOf(80))

    val reservable2 = updatedQueue2.queue(1)
    reservable2.isReservedBy("ContextB") should be(true)
    reservable2.value.amount should be(BigDecimal.valueOf(20))

    val reservable3 = updatedQueue2.queue(2)
    reservable3.isReservedBy("ContextB") should be(true)
    reservable3.value.amount should be(BigDecimal.valueOf(30))

    val reservable4 = updatedQueue2.queue(3)
    reservable4.isReserved should be(false)
    reservable4.value.amount should be(BigDecimal.valueOf(20))
  }

  it should "not allow reserving an already reserved item by a different context" in {
    var queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(100)))

    val (_, updatedQueue1) = queue.reserve(BigDecimal.valueOf(100), "ContextA")

    val (amountRemaining, updatedQueue2) = updatedQueue1.reserve(BigDecimal.valueOf(50), "ContextB")

    amountRemaining shouldBe defined
    amountRemaining.get should be(BigDecimal.valueOf(50)) // Unable to reserve any amount

    // Ensure that the item is still reserved by ContextA
    val reservable = updatedQueue2.queue.head
    reservable.isReservedBy("ContextA") should be(true)
    reservable.isReserved should be(true)
  }

  it should "reserve by splitting across multiple items" in {
    var queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(60)))
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(40)))

    val (amountRemaining, updatedQueue) = queue.reserve(BigDecimal.valueOf(80), "ContextA")

    amountRemaining should be(None)

    // Check that 60 from the first item and 20 from the second item are reserved
    val reservable1 = updatedQueue.queue.head
    reservable1.isReservedBy("ContextA") should be(true)
    reservable1.value.amount should be(BigDecimal.valueOf(60))

    val reservable2 = updatedQueue.queue(1)
    reservable2.isReservedBy("ContextA") should be(true)
    reservable2.value.amount should be(BigDecimal.valueOf(20))

    // Remaining unreserved amount from the second item
    val reservable3 = updatedQueue.queue(2)
    reservable3.isReserved should be(false)
    reservable3.value.amount should be(BigDecimal.valueOf(20))
  }

  it should "handle insufficient resources gracefully" in {
    var queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(30)))
    queue = queue.add(Donation("Donor", BigDecimal.valueOf(20)))

    val (amountRemaining, updatedQueue) = queue.reserve(BigDecimal.valueOf(100), "ContextA")

    amountRemaining shouldBe defined
    amountRemaining.get should be(BigDecimal.valueOf(50))

    // Check that all available resources are reserved
    updatedQueue.queue.foreach { reservable =>
      reservable.isReservedBy("ContextA") should be(true)
    }
  }

  it should "not reserve when no resources are available" in {
    val queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")

    val (amountRemaining, updatedQueue) = queue.reserve(BigDecimal.valueOf(50), "ContextA")

    amountRemaining shouldBe defined
    amountRemaining.get should be(BigDecimal.valueOf(50))
    updatedQueue.queue shouldBe empty
  }

  it should "allow adding resources after attempting to reserve" in {
    var queue = ReservableQueue[DonationPart, BigDecimal, String]("MainAccount")

    // Attempt to reserve before adding any resources
    val (_, updatedQueue1) = queue.reserve(BigDecimal.valueOf(50), "ContextA")

    // Adding resources after reservation attempt
    queue = updatedQueue1.add(Donation("Donor", BigDecimal.valueOf(100)))

    val (amountRemaining, updatedQueue2) = queue.reserve(BigDecimal.valueOf(50), "ContextA")

    amountRemaining should be(None)

    val reservable = updatedQueue2.queue.head
    reservable.isReservedBy("ContextA") should be(true)
    reservable.value.amount should be(BigDecimal.valueOf(50))
  }
