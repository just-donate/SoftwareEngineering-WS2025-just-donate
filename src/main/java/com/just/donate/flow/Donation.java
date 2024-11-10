package com.just.donate.flow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single donation. A donation can be split into multiple parts, e.g. for different purposes.
 * The donation is always made by a donor and has a date when it was made.
 */
public class Donation {

    private final String donor;
    private final LocalDateTime donationDate;
    private final List<DonationPart> parts;

    protected Donation(String donor, BigDecimal amount, LocalDateTime donationDate) {
        this.donor = donor;
        this.parts = new ArrayList<>();
        this.parts.add(new DonationPart(amount));
        this.donationDate = donationDate;
    }

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
