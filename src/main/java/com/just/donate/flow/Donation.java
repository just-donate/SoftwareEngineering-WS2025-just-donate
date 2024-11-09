package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class Donation {
    private final UUID id;
    // TODO: refactor these two out?
    private final String name;
    private final String email;
    private final Date date;
    private final BigDecimal amount;

    // TODO: track parts?

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Date getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Donation(String name, String email, java.util.Date today, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.date = today;
        this.amount = amount;
    }
}
