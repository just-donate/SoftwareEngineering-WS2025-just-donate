package com.just.donate.flow;

import java.util.ArrayList;
import java.util.List;

public class Account {

    private final String name;
    private final List<BoundDonationQueue> boundDonations;
    private final UnboundDonationQueue unboundDonations;

    public Account(String name) {
        this.name = name;
        this.boundDonations = new ArrayList<>();
        this.unboundDonations = new UnboundDonationQueue();
    }

    public String getName() {
        return name;
    }
}
