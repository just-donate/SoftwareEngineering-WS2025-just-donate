package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.just.donate.utils.Utils.lessOrEqual;

public class Expense {

    private final String description;
    private final BigDecimal amount;
    private final List<DonationPart> paidBy;
    private final Optional<String> earMarking;

    public Expense(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
        this.paidBy = new ArrayList<>();
        this.earMarking = Optional.empty();
    }

    public Expense(String description, BigDecimal amount, String boundTo) {
        this.description = description;
        this.amount = amount;
        this.paidBy = new ArrayList<>();
        this.earMarking = Optional.of(boundTo);
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Optional<DonationPart> payWith(DonationPart donationPart) {
        if (paidBy.contains(donationPart)) {
            throw new IllegalStateException("Donation part already used to pay this expense");
        }

        return donationPart.spendOn(this);
    }

    public boolean isPaid() {
        BigDecimal paid = paidBy.stream()
                .map(DonationPart::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return lessOrEqual(paid, amount);
    }

    public boolean isBound() {
        return earMarking.isPresent();
    }

    public String getEarmarking() {
        if (earMarking.isPresent()) {
            return earMarking.get();
        } else {
            throw new IllegalStateException("Expense is not bound to any earmarking");
        }
    }

}
