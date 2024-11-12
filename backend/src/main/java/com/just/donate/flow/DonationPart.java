package com.just.donate.flow;

import com.just.donate.utils.Splittable;

import java.math.BigDecimal;
import java.util.Optional;

import static com.just.donate.utils.Utils.*;

/**
 * Represents a part of a donation that can be spent on an expense. It is mainly used to keep track of the amount
 * of the donation that has been split up and spent on different expenses.
 */
public class DonationPart implements Splittable<DonationPart, BigDecimal> {

    private final BigDecimal amount;

    public DonationPart(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public Split<DonationPart, BigDecimal> splitOf(BigDecimal bigDecimal) {
        if (equal(bigDecimal, BigDecimal.ZERO)) {
            return new Split<>(Optional.empty(), Optional.of(this), Optional.empty());
        } else if (less(bigDecimal, amount)) {
            BigDecimal remainingAmount = amount.subtract(bigDecimal);
            return new Split<>(Optional.of(new DonationPart(bigDecimal)), Optional.of(new DonationPart(remainingAmount)), Optional.empty());
        } else if (bigDecimal.equals(amount)) {
            return new Split<>(Optional.of(this), Optional.empty(), Optional.empty());
        } else if (greater(bigDecimal, amount) && greater(amount, BigDecimal.ZERO)) {
            BigDecimal notPaid = bigDecimal.subtract(amount);
            return new Split<>(Optional.of(this), Optional.empty(), Optional.of(notPaid));
        } else {
            throw new IllegalStateException("Should not happen?");
        }
    }
    
    @Override
    public String toString() {
        return amount.toString();
    }

}
