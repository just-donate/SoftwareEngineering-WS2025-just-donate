package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.Optional;

import com.just.donate.utils.Tuple;

public class DonationPart {
    private final Donation donation;
    private BigDecimal amount;
    private Optional<Expense> expense = Optional.empty();
    private Optional<Tuple<Account, String>> destinationMark = Optional.empty();

    public DonationPart(Donation donation, BigDecimal amount) {
        this.donation = donation;
        this.amount = amount;
    }

    public static DonationPart fromDonation(Donation donation) {
        return new DonationPart(donation, donation.getAmount());
    }

    public Donation getDonation() {
        return donation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Optional<Tuple<Account, String>> getDestinationMark() {
        return destinationMark;
    }

    public void setExpense(Expense expense) {
        this.expense = Optional.of(expense);
        expense.addDonationPart(this);
    }
}
