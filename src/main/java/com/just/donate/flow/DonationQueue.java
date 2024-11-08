package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Queue;

class DonationQueue {

    private final Queue<DonationPart> donations;
    private BigDecimal negativeBalance = BigDecimal.ZERO;

    public DonationQueue() {
        this.donations = new ArrayDeque<>();
    }

    public boolean add(DonationPart donation) {
        return donations.add(donation);
    }

    public boolean add(Donation donation) {
        return donations.addAll(donation.getParts());
    }

    public BigDecimal totalBalance() {
        return donations.stream()
                .map(DonationPart::getAmount)
                .reduce(negativeBalance, BigDecimal::add);
    }

    protected boolean spend(BigDecimal spending) {
        // TODO: Implement spending
        return false;
    }
}
