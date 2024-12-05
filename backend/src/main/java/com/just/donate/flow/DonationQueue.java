package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.List;

class DonationQueue {

    private final ReservableQueue<DonationPart, BigDecimal, Account> donations;
    private final BigDecimal negativeBalance = BigDecimal.ZERO;

    public DonationQueue(Account account) {
        this.donations = new ReservableQueue<>(account);
    }

    public void add(DonationPart donation) {
        donations.add(donation);
    }
    
    public void addAll(List<DonationPart> donations) {
        donations.forEach(this::add);
    }

    public BigDecimal totalBalance() {
        return donations.getQueue().map(r -> r.getValue().getAmount()).foldLeft(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public ReservableQueue<DonationPart, BigDecimal, Account> getDonationsQueue() {
        return donations;
    }
}
