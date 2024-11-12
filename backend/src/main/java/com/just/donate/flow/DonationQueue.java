package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.*;

class DonationQueue {

    private final Queue<DonationPart> donations;
    private BigDecimal negativeBalance = BigDecimal.ZERO;
    private final List<Expense> outstandingExpenses;

    public DonationQueue() {
        this.donations = new ArrayDeque<>();
        this.outstandingExpenses = new ArrayList<>();
    }

    public boolean add(DonationPart donation) {
        return donations.add(donation);
    }

    public boolean add(Donation donation) {
        if (outstandingExpenses.isEmpty()) {
            return donations.add(new DonationPart(donation.getAmount()));
        } else {
            return false;
        }
    }

    public BigDecimal totalBalance() {
        return donations.stream()
                .map(DonationPart::getAmount)
                .reduce(negativeBalance, BigDecimal::add);
    }

    /**
     * Spend the given expense from the donation queue. If the expense is not fully covered by the donations,
     * the remaining amount is added to the negative balance.
     * @param expense The expense to spend from the donation queue.
     */
    protected void spendGoMinus(Expense expense) {
        while (!expense.isPaid() && !donations.isEmpty()) {
            DonationPart donationPart = donations.poll();
            Optional<DonationPart> remaining = expense.payWith(donationPart);
            if (remaining.isPresent()) {
                // TODO: this is not really working as it should put the remaining back into the front of the queue
                this.add(remaining.get());
                break;
            }
        }


    }
}
