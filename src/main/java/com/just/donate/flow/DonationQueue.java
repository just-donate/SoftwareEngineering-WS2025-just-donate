package com.just.donate.flow;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;

abstract class DonationQueue extends ArrayDeque<DonationPart> {
    private BigDecimal totalPending;

    public BigDecimal getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(BigDecimal totalPending) {
        this.totalPending = totalPending;
    }

    public DonationQueue() {
        this.totalPending = BigDecimal.ZERO;
    }

    public void addDonation(DonationPart donation) {
        this.add(donation);
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("Y-MM-dd");

    public void print() {
        var iter = this.descendingIterator();
        while (iter.hasNext()) {
            DonationPart part = iter.next();
            BigDecimal amount = part.getAmount();
            Donation donation = part.getDonation();
            System.out.println(
                    String.format("%s from %s (Email: %s) on %s",
                            amount.toString(),
                            donation.getName(),
                            donation.getEmail(),
                            dateFormat.format(donation.getDate())));
        }
    }
}
