package com.just.donate.flow;

import io.vavr.Tuple2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Account {

    private final String name;
    private final List<Tuple2<String, DonationQueue>> boundDonations;
    private final DonationQueue unboundDonations;

    protected Account(String name) {
        this.name = name;
        this.boundDonations = new ArrayList<>();
        this.unboundDonations = new DonationQueue();
    }

    public String getName() {
        return name;
    }

    public boolean donate(String donor, BigDecimal amount) {
        Donation donation = new Donation(donor, amount);
        return unboundDonations.add(donation);
    }

    public boolean donate(String donor, BigDecimal amount, String boundTo) {
        Optional<DonationQueue> boundQueue = boundDonations.stream()
                .filter(bound -> bound._1.equals(boundTo))
                .map(Tuple2::_2)
                .findFirst();

        if (boundQueue.isPresent()) {
            Donation donation = new Donation(donor, amount);
            return boundQueue.get().add(donation);
        } else {
            return false;
        }
    }

    protected void addEarmarking(String earmarking) {
        boundDonations.add(new Tuple2<>(earmarking, new DonationQueue()));
    }

    protected BigDecimal totalBalance() {
        return unboundDonations.totalBalance()
                .add(boundDonations.stream()
                        .map(bound -> bound._2.totalBalance())
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    protected boolean spend(BigDecimal spending) {
        return unboundDonations.spend(spending);
    }
}
