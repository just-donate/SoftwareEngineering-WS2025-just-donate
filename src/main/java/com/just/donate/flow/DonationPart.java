package com.just.donate.flow;

import java.math.BigDecimal;

public class DonationPart {

    private BigDecimal amount;
    private boolean spent;

    protected DonationPart(BigDecimal amount) {
        this.amount = amount;
        this.spent = false;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isSpent() {
        return spent;
    }

    protected DonationPart spend(BigDecimal spending) {
        if (spent) {
            throw new IllegalStateException("Donation part already spent");
        }

        if (spending.compareTo(amount) > 0) {
            throw new IllegalArgumentException("Spending amount exceeds donation part amount.");
        }

        this.spent = true;
        this.amount = spending;
        return new DonationPart(amount.subtract(spending));
    }
}
