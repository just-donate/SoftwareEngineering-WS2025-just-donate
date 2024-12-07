package com.just.donate.models;


public class AccountReserveTest {
    /*
    private Account upstream1;
    private Account upstream2;
    private Account upstream3;
    
    private Account upupstream1;
    private Account upupstream2;
    
    private Account downstream;
    
    private void refreshAccounts() {
        upstream1 = new Account("Upstream 1")
        upstream2 = new Account("Upstream 2");
        upstream3 = new Account("Upstream 3");
        
        upupstream1 = new Account("UpUpstream 1");
        upupstream2 = new Account("UpUpstream 2");
        
        downstream = new Account("Downstream");
    }
    
    @BeforeEach
    void beforeEach() {
        refreshAccounts();
    }
    
    @Test
    void untouchedAccountIsUnreserved() {
        boolean nonReserved = upstream1.getUnboundDonations().donationQueue()
                .getQueue().forAll(dp -> !dp.isReserved());
        assertTrue(nonReserved);
        
        for (Tuple2<String, DonationQueue> dq : upstream1.getBoundDonations()) {
            nonReserved = dq._2().getDonationsQueue()
                    .getQueue().forAll(dp -> !dp.isReserved());
            assertTrue(nonReserved);
        }
        
        nonReserved = downstream.getUnboundDonations().getDonationsQueue()
                .getQueue().forAll(dp -> !dp.isReserved());
        assertTrue(nonReserved);
        
        for (Tuple2<String, DonationQueue> dq : downstream.getBoundDonations()) {
            nonReserved = dq._2().getDonationsQueue()
                    .getQueue().forAll(dp -> !dp.isReserved());
            assertTrue(nonReserved);
        }
    }
    */
    /**
     * Checks if the account has multiple upstreams it reserves the oldest donation independent of upstream.
     */
    /*
    @Test
    void reservesAlwaysTheOldestFirst() {
        upstream1.donate(new Donation("Donor 1", BigDecimal.valueOf(100), 
                LocalDateTime.of(2021, 1, 2, 0, 0)));
        upstream2.donate(new Donation("Donor 2", BigDecimal.valueOf(100),
                LocalDateTime.of(2021, 1, 3, 0, 0)));
        
        downstream.donate(new Donation("Donor 3", BigDecimal.valueOf(100),
                LocalDateTime.of(2021, 1, 1, 0, 0)));
        
        // This should now reserve everything in downstream and upstream1 when 200 is needed
        downstream.spend(new Expense("Expense 1", BigDecimal.valueOf(200)));
        
        assertEquals(0, downstream.getUnboundDonations().getDonationsQueue().getQueue().size());
        assertEquals(1, upstream1.getUnboundDonations().getDonationsQueue().getQueue().size());
        assertEquals(1, upstream2.getUnboundDonations().getDonationsQueue().getQueue().size());
        
        assertTrue(upstream1.getUnboundDonations().getDonationsQueue().getQueue().get(0).isReserved());
        assertFalse(upstream2.getUnboundDonations().getDonationsQueue().getQueue().get(0).isReserved());
    }
    
     */
    
    /**
     * Checks if the account has multiple upstreams it reserves the oldest donation independent of upstream in
     * bound donation queues.
     */
    // @Test
    void reservesAlwaysTheOldestFirstInBoundDonations() {
        
    }
    
    /**
     * Checks if the account has multiple upstreams it reserves the oldest donation independent of upstream and
     * hierarchical levels.
     */
    // @Test
    void reservesAlwaysTheOldestFirstAcrossMultipleLevels() {
        
    }
    
    /**
     * Checks if the account has multiple upstreams it reserves the oldest donation independent of upstream and
     * hierarchical levels in bound donation queues.
     */
    // @Test
    void reservesAlwaysTheOldestFirstAcrossMultipleLevelsInBoundDonations() {
        
    }
    
}
