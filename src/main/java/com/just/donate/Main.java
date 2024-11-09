package com.just.donate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

import com.just.donate.flow.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        test1();
    }

    private static LocalDate currentDate = LocalDate.now();

    private static Date getDateWithOffset(long amount, TemporalUnit unit) {
        return Date.from(currentDate.minus(amount, unit).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static void test1() {
        Organisation org = new Organisation("New Roots e.V.");
        Account paypal = new Account("PayPal");
        Account germanAccount = new Account("German Account");
        Account kenyanAccount = new Account("Kenyan Account");
        org.addAccount(paypal);
        org.addAccount(germanAccount);
        org.addAccount(kenyanAccount);

        germanAccount.addIncomingConnection(paypal);
        kenyanAccount.addIncomingConnection(germanAccount);

        Runnable printOrg = () -> {
            System.out.println("====== Organisation: " + org.getName());
            org.printQueues();
            System.out.println();
        };

        printOrg.run();

        paypal.addDonation(
                new Donation("a", "a@example.com", getDateWithOffset(4, ChronoUnit.MONTHS), BigDecimal.valueOf(25)),
                new UnboundedDonationType());
        paypal.addDonation(
                new Donation("b", "b@example.com", getDateWithOffset(3, ChronoUnit.MONTHS), BigDecimal.valueOf(25)),
                new UnboundedDonationType());
        paypal.addDonation(
                new Donation("c", "c@example.com", getDateWithOffset(2, ChronoUnit.MONTHS), BigDecimal.valueOf(25)),
                new UnboundedDonationType());
        paypal.addDonation(
                new Donation("d", "d@example.com", getDateWithOffset(1, ChronoUnit.MONTHS), BigDecimal.valueOf(25)),
                new UnboundedDonationType());

        printOrg.run();

        paypal.makeTransaction(BigDecimal.valueOf(575, 1), kenyanAccount, new UnboundedDonationType());

        printOrg.run();

        kenyanAccount.addExpense(new Expense(getDateWithOffset(1, ChronoUnit.WEEKS), BigDecimal.valueOf(80)),
                new UnboundedDonationType());

        printOrg.run();

        paypal.makeTransaction(BigDecimal.valueOf(255, 1), kenyanAccount, new UnboundedDonationType());

        printOrg.run();
    }
}
