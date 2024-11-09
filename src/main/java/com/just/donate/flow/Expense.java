package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Expense {
    private final UUID id;
    private final Date date;
    private final BigDecimal amount;
    private List<DonationPart> donationParts;

    public Expense(Date date, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.date = date;
        this.amount = amount;
        this.donationParts = new ArrayList<>();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void addDonationPart(DonationPart donationPart) {
        this.donationParts.add(donationPart);
    }
}
