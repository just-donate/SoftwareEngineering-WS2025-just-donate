package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.Optional;

import static com.just.donate.utils.Utils.less;

/**
 * Represents a part of a donation that can be spent on an expense. It is mainly used to keep track of the amount
 * of the donation that has been split up and spent on different expenses.
 */
public class DonationPart {

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

    /**
     * Pay off the given expense with the donation part. If the donation part is bigger than the expense, the remaining
     * amount is returned as a new donation part.
     * @param expense The expense to pay off with the donation part.
     * @return The remaining amount as a new donation part, if the donation part is bigger than the expense.
     */
    protected Optional<DonationPart> spendOn(Expense expense) {
        if (spent) {
            throw new IllegalStateException("Donation part already spent");
        }

        if (expense.isPaid()) {
            throw new IllegalStateException("Expense already paid");
        }

        BigDecimal expenseAmount = expense.getAmount();
        expense.payWith(this);
        spent = true;

        if (less(amount, expenseAmount)) {
            return Optional.of(new DonationPart(amount.subtract(expenseAmount)));
        } else {
            return Optional.empty();
        }
    }
}
