package com.just.donate.utils;

import com.just.donate.flow.DonationPart;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ReservableQueueTest {

    @Test
    public void testAddAndReserveExactAmount() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();
        queue.add(new DonationPart(BigDecimal.valueOf(100)));

        BigDecimal amountRemaining = queue.reserve(BigDecimal.valueOf(100), "ContextA");
        assertNull(amountRemaining);

        // Check that the first item is reserved by ContextA
        Reservable<DonationPart, BigDecimal, String> reservable = queue.getQueue().get(0);
        assertTrue(reservable.isReserved());
        assertTrue(reservable.isReservedBy("ContextA"));
        assertEquals(BigDecimal.valueOf(100), reservable.getValue().getAmount());
    }

    @Test
    public void testReserveLessThanAvailableAmount() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();
        queue.add(new DonationPart(BigDecimal.valueOf(100)));

        BigDecimal amountRemaining = queue.reserve(BigDecimal.valueOf(40), "ContextA");
        assertNull(amountRemaining);

        // Check that the first item is reserved by ContextA with amount 40
        Reservable<DonationPart, BigDecimal, String> reservable1 = queue.getQueue().get(0);
        assertTrue(reservable1.isReserved());
        assertTrue(reservable1.isReservedBy("ContextA"));
        assertEquals(BigDecimal.valueOf(40), reservable1.getValue().getAmount());

        // Check that the second item is unreserved with amount 60
        Reservable<DonationPart, BigDecimal, String> reservable2 = queue.getQueue().get(1);
        assertFalse(reservable2.isReserved());
        assertEquals(BigDecimal.valueOf(60), reservable2.getValue().getAmount());
    }

    @Test
    public void testReserveMoreThanAvailableAmount() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();
        queue.add(new DonationPart(BigDecimal.valueOf(50)));

        BigDecimal amountRemaining = queue.reserve(BigDecimal.valueOf(100), "ContextA");
        assertEquals(BigDecimal.valueOf(50), amountRemaining);

        // Check that the item is reserved by ContextA with amount 50
        Reservable<DonationPart, BigDecimal, String> reservable = queue.getQueue().get(0);
        assertTrue(reservable.isReserved());
        assertTrue(reservable.isReservedBy("ContextA"));
        assertEquals(BigDecimal.valueOf(50), reservable.getValue().getAmount());
    }

    @Test
    public void testMultipleReservesDifferentContexts() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();
        queue.add(new DonationPart(BigDecimal.valueOf(100)));
        queue.add(new DonationPart(BigDecimal.valueOf(50)));

        BigDecimal amountRemaining1 = queue.reserve(BigDecimal.valueOf(80), "ContextA");
        assertNull(amountRemaining1);

        BigDecimal amountRemaining2 = queue.reserve(BigDecimal.valueOf(50), "ContextB");
        assertNull(amountRemaining2);

        // Check reservations
        Reservable<DonationPart, BigDecimal, String> reservable1 = queue.getQueue().get(0);
        assertTrue(reservable1.isReservedBy("ContextA"));
        assertEquals(BigDecimal.valueOf(80), reservable1.getValue().getAmount());

        Reservable<DonationPart, BigDecimal, String> reservable2 = queue.getQueue().get(1);
        assertTrue(reservable2.isReservedBy("ContextB"));
        assertEquals(BigDecimal.valueOf(20), reservable2.getValue().getAmount());

        Reservable<DonationPart, BigDecimal, String> reservable3 = queue.getQueue().get(2);
        assertTrue(reservable3.isReservedBy("ContextB"));
        assertEquals(BigDecimal.valueOf(30), reservable3.getValue().getAmount());

        Reservable<DonationPart, BigDecimal, String> reservable4 = queue.getQueue().get(3);
        assertFalse(reservable4.isReserved());
        assertEquals(BigDecimal.valueOf(20), reservable4.getValue().getAmount());
    }

    @Test
    public void testReserveAlreadyReservedItem() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();
        queue.add(new DonationPart(BigDecimal.valueOf(100)));

        queue.reserve(BigDecimal.valueOf(100), "ContextA");

        // Try to reserve again with a different context
        BigDecimal amountRemaining = queue.reserve(BigDecimal.valueOf(50), "ContextB");
        assertEquals(BigDecimal.valueOf(50), amountRemaining); // Unable to reserve any amount

        // Ensure that the item is still reserved by ContextA
        Reservable<DonationPart, BigDecimal, String> reservable = queue.getQueue().get(0);
        assertTrue(reservable.isReservedBy("ContextA"));
    }

    @Test
    public void testReserveWithSplittingAcrossMultipleItems() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();
        queue.add(new DonationPart(BigDecimal.valueOf(60)));
        queue.add(new DonationPart(BigDecimal.valueOf(40)));

        BigDecimal amountRemaining = queue.reserve(BigDecimal.valueOf(80), "ContextA");
        assertNull(amountRemaining);

        // Check that 60 from the first item and 20 from the second item are reserved
        Reservable<DonationPart, BigDecimal, String> reservable1 = queue.getQueue().get(0);
        assertTrue(reservable1.isReservedBy("ContextA"));
        assertEquals(BigDecimal.valueOf(60), reservable1.getValue().getAmount());

        Reservable<DonationPart, BigDecimal, String> reservable2 = queue.getQueue().get(1);
        assertTrue(reservable2.isReservedBy("ContextA"));
        assertEquals(BigDecimal.valueOf(20), reservable2.getValue().getAmount());

        // Remaining unreserved amount from the second item
        Reservable<DonationPart, BigDecimal, String> reservable3 = queue.getQueue().get(2);
        assertFalse(reservable3.isReserved());
        assertEquals(BigDecimal.valueOf(20), reservable3.getValue().getAmount());
    }

    @Test
    public void testInsufficientResources() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();
        queue.add(new DonationPart(BigDecimal.valueOf(30)));
        queue.add(new DonationPart(BigDecimal.valueOf(20)));

        BigDecimal amountRemaining = queue.reserve(BigDecimal.valueOf(100), "ContextA");
        assertEquals(BigDecimal.valueOf(50), amountRemaining);

        // Check that all available resources are reserved
        for (Reservable<DonationPart, BigDecimal, String> reservable : queue.getQueue()) {
            assertTrue(reservable.isReservedBy("ContextA"));
        }
    }

    @Test
    public void testNoAvailableResources() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();

        BigDecimal amountRemaining = queue.reserve(BigDecimal.valueOf(50), "ContextA");
        assertEquals(BigDecimal.valueOf(50), amountRemaining);
        assertTrue(queue.getQueue().isEmpty());
    }

    @Test
    public void testAddingAfterReserving() {
        ReservableQueue<DonationPart, BigDecimal, String> queue = new ReservableQueue<>();
        queue.reserve(BigDecimal.valueOf(50), "ContextA"); // Should have no effect

        queue.add(new DonationPart(BigDecimal.valueOf(100)));
        BigDecimal amountRemaining = queue.reserve(BigDecimal.valueOf(50), "ContextA");
        assertNull(amountRemaining);

        Reservable<DonationPart, BigDecimal, String> reservable = queue.getQueue().get(0);
        assertTrue(reservable.isReservedBy("ContextA"));
        assertEquals(BigDecimal.valueOf(50), reservable.getValue().getAmount());
    }


}
