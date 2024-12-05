package com.just.donate.flow;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountReserveTest {
    
    private Account upstream;
    private Account downstream;
    
    private void refreshAccounts() {
        upstream = new Account("Upstream");
        downstream = new Account("Downstream");
        upstream.addOutgoingFlow(downstream);
    }
    
    @BeforeEach
    void beforeEach() {
        refreshAccounts();
    }
    
    @Test
    void untouchedAccountIsUnreserved() {
        boolean nonReserved = upstream.getUnboundDonations().getDonationsQueue()
                .getQueue().forAll(dp -> !dp.isReserved());
        assertTrue(nonReserved);
        
        for (Tuple2<String, DonationQueue> dq : upstream.getBoundDonations()) {
            nonReserved = dq._2().getDonationsQueue()
                    .getQueue().forAll(dp -> !dp.isReserved());
            assertTrue(nonReserved);
        }
    }
}
