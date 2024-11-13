package com.just.donate.flow;

import com.just.donate.utils.Splittable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.just.donate.utils.Utils.*;

/**
 * Represents a part of a donation that can be spent on an expense. It is mainly used to keep track of the amount
 * of the donation that has been split up and spent on different expenses.
 */
public class DonationPart implements Splittable<DonationPart, BigDecimal>, java.util.Comparator<DonationPart> {

    private final BigDecimal amount;
    private final LocalDateTime donationTime;

    public DonationPart(BigDecimal amount, LocalDateTime donationTime) {
        this.amount = amount;
        this.donationTime = donationTime;
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
            return new Split<>(
                    Optional.of(new DonationPart(bigDecimal, donationTime)),
                    Optional.of(new DonationPart(remainingAmount, donationTime)),
                    Optional.empty());

        } else if (bigDecimal.equals(amount)) {
            return new Split<>(
                    Optional.of(this),
                    Optional.empty(),
                    Optional.empty());

        } else if (greater(bigDecimal, amount) && greater(amount, BigDecimal.ZERO)) {
            BigDecimal notPaid = bigDecimal.subtract(amount);
            return new Split<>(
                    Optional.of(this),
                    Optional.empty(),
                    Optional.of(notPaid));

        } else {
            throw new IllegalStateException("Should not happen?");
        }
    }

    @Override
    public String toString() {
        return amount.toString();
    }

    @Override
    public int compare(DonationPart o1, DonationPart o2) {
        return o1.donationTime.compareTo(o2.donationTime);
    }
}
