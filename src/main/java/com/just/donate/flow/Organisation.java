package com.just.donate.flow;

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
}
