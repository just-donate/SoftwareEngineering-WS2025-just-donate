package com.just.donate;


import com.just.donate.flow.Organisation;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {

        Organisation organisation = new Organisation("Just Donate");

        organisation.addAccount("Paypal");
        organisation.addAccount("Bank");

        organisation.addEarmarking("School");
        organisation.addEarmarking("Hospital");

        organisation.getAccounts().forEach(account -> {
            account.donate("Alice", BigDecimal.valueOf(100));
            account.donate("Bob", BigDecimal.valueOf(200), "School");
            account.donate("Charlie", BigDecimal.valueOf(300), "Hospital");
        });

        System.out.println("Total balance: " + organisation.totalBalance());

    }
}