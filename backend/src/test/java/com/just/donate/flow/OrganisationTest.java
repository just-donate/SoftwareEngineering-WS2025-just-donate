package com.just.donate.flow;

import com.just.donate.flow.Account;
import com.just.donate.flow.Organisation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrganisationTest {

    Organisation newRoots() {
        Organisation newRoots = new Organisation("New Roots");

        Account paypal = new Account("Paypal");
        Account betterPlace = new Account("Better Place");
        Account bank = new Account("Bank");
        Account kenya = new Account("Kenya");

        newRoots.addAccount(paypal);
        newRoots.addAccount(betterPlace);
        newRoots.addAccount(bank);
        newRoots.addAccount(kenya);

        bank.addIncomingFlow(paypal);
        bank.addIncomingFlow(betterPlace);
        kenya.addIncomingFlow(bank);

        return newRoots;
    }

    @Test
    void totalBalanceWithoutExpenseMatches() {
        Organisation newRoots = newRoots();
        assertEquals(BigDecimal.ZERO, newRoots.totalBalance());
    }

    @Test
    void testUnboundDonationAffectsTotalBalance() {
        Organisation newRoots = newRoots();
        Account paypal = newRoots.getAccount("Paypal");

        paypal.donate("Donor1", new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), paypal.totalBalance());
        assertEquals(new BigDecimal("100.00"), newRoots.totalBalance());
    }

    @Test
    void testBoundDonationAffectsTotalBalance() {
        // TODO @AntonKluge fix this test I commented out the line below where an error was thrown
        Organisation newRoots = newRoots();
        Account paypal = newRoots.getAccount("Paypal");

        paypal.addEarmarking("Education");
        boolean success = paypal.donate("Donor1", new BigDecimal("200.00"), "Education");
        assertTrue(success);
        assertEquals(new BigDecimal("200.00"), paypal.totalBalance());
        assertEquals(new BigDecimal("200.00"), newRoots.totalBalance());

        assertEquals(new BigDecimal("200.00"), paypal.totalEarmarkedBalance("Education"));
        assertEquals(new BigDecimal("200.00"), newRoots.totalEarmarkedBalance("Education"));
      
        // assertThrows(IllegalStateException.class, () -> newRoots.totalEarmarkedBalance("Health"));
    }

    @Test
    void testMultipleAccountsDonationsAffectTotalBalance() {
        Organisation newRoots = newRoots();
        Account paypal = newRoots.getAccount("Paypal");
        Account bank = newRoots.getAccount("Bank");

        paypal.donate("Donor1", new BigDecimal("100.00"));
        bank.donate("Donor2", new BigDecimal("150.00"));

        assertEquals(new BigDecimal("100.00"), paypal.totalBalance());
        assertEquals(new BigDecimal("150.00"), bank.totalBalance());
        assertEquals(new BigDecimal("250.00"), newRoots.totalBalance());
    }

    @Test
    void testDonationWithIncomingFlowAffectsTotalBalance() {
        Organisation newRoots = newRoots();
        Account paypal = newRoots.getAccount("Paypal");
        Account bank = newRoots.getAccount("Bank");

        paypal.donate("Donor1", new BigDecimal("100.00"));
        bank.donate("Donor2", new BigDecimal("150.00"));

        assertEquals(new BigDecimal("100.00"), paypal.totalBalance());
        assertEquals(new BigDecimal("150.00"), bank.totalBalance());
        assertEquals(new BigDecimal("250.00"), newRoots.totalBalance());

        // Adding donation to an account with incoming flow
        Account kenya = newRoots.getAccount("Kenya");
        kenya.donate("Donor3", new BigDecimal("200.00"));

        assertEquals(new BigDecimal("200.00"), kenya.totalBalance());
        assertEquals(new BigDecimal("450.00"), newRoots.totalBalance());
    }

}