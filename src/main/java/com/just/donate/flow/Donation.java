package com.just.donate.flow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Donation {

    private final String donor;
    private final LocalDateTime donationDate;
    private final List<DonationPart> parts;

    public Donation(String donor, BigDecimal amount) {
        this.donor = donor;
        this.parts = new ArrayList<>();
        this.parts.add(new DonationPart(amount));
        this.donationDate = LocalDateTime.now();
    }

    public String getDonor() {
        return donor;
    }

    public LocalDateTime getDonationDate() {
        return donationDate;
    }

    public List<DonationPart> getParts() {
        return parts;
    }

    public BigDecimal getAmount() {
        return parts.stream().map(DonationPart::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return String.format("Donation from %s on %s", donor, donationDate);
    }

}
