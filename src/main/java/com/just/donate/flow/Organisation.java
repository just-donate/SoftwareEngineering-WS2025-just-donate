package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Organisation {

    private final String name;
    private final List<Account> accounts;

    public Organisation(String name) {
        this.name = name;
        this.accounts = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(String name) {
        accounts.add(new Account(name));
    }

    public void addEarmarking(String earmarking) {
        accounts.forEach(account -> account.addEarmarking(earmarking));
    }

    public BigDecimal totalBalance() {
        return accounts.stream().map(Account::totalBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
