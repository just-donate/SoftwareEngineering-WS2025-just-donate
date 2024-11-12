package com.just.donate.flow;

import com.just.donate.utils.Splittable;

import java.math.BigDecimal;
import java.util.Optional;

import static com.just.donate.utils.Utils.less;
import static com.just.donate.utils.Utils.lessOrEqual;

/**
 * Represents a part of a donation that can be spent on an expense. It is mainly used to keep track of the amount
 * of the donation that has been split up and spent on different expenses.
 */
public class DonationPart implements Splittable<DonationPart, BigDecimal> {

    private final BigDecimal amount;
    private boolean spent;

    protected DonationPart(BigDecimal amount) {
        this.amount = amount;
        this.spent = false;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Check if the donation part has been spent on an expense. If it has, it cannot be spent again and
     * is part of the expense that it was spent on.
     * @return True if the donation part has been spent, false otherwise.
     */
    public boolean isSpent() {
        return spent;
    }

    @Override
    public Split<DonationPart> splitOf(BigDecimal bigDecimal) {
        if (less(bigDecimal, amount)) {
            BigDecimal remainingAmount = amount.subtract(bigDecimal);
            return Split.withRemaining(new DonationPart(bigDecimal), new DonationPart(remainingAmount));
        } else if (bigDecimal.equals(amount)) {
            return Split.noRemaining(this);
        } else {
            throw new IllegalStateException("More split of than available!");
        }
    }

    @Override
    public boolean canSplit(BigDecimal bigDecimal) {
        return lessOrEqual(BigDecimal.ZERO, bigDecimal) && lessOrEqual(bigDecimal, amount);
    }
}
