package com.just.donate.models;


class DonationPartTest {

    /*
    @Test
    void testBasisDonationPart() {
        DonationPart donationPart = new DonationPart(BigDecimal.valueOf(100), LocalDateTime.now());
        assertEquals(donationPart.getAmount(), BigDecimal.valueOf(100));

        DonationPart donationPart2 = new DonationPart(BigDecimal.valueOf(0), LocalDateTime.now());
        assertEquals(donationPart2.getAmount(), BigDecimal.valueOf(0));

        DonationPart donationPart3 = new DonationPart(BigDecimal.valueOf(-100), LocalDateTime.now());
        assertEquals(donationPart3.getAmount(), BigDecimal.valueOf(-100));
    }

    @Test
    void testSplitNothingOf() {
        DonationPart donationPart = new DonationPart(BigDecimal.valueOf(100), LocalDateTime.now());
        Splittable.Split<DonationPart, BigDecimal> split = donationPart.splitOf(BigDecimal.valueOf(0));

        assertTrue(split.getSplit().isEmpty());
        assertTrue(split.getOpen().isEmpty());
        assertTrue(split.getRemain().isPresent());
        assertEquals(split.getRemain().get().getAmount(), BigDecimal.valueOf(100));
        assertTrue(split.fullRemain());
    }

    @Test
    void splitALittleOf() {
        DonationPart donationPart = new DonationPart(BigDecimal.valueOf(100), LocalDateTime.now());
        Splittable.Split<DonationPart, BigDecimal> split = donationPart.splitOf(BigDecimal.valueOf(50));

        assertTrue(split.getSplit().isPresent());
        assertEquals(split.getSplit().get().getAmount(), BigDecimal.valueOf(50));
        assertTrue(split.getOpen().isEmpty());
        assertTrue(split.getRemain().isPresent());
        assertEquals(split.getRemain().get().getAmount(), BigDecimal.valueOf(50));
        assertTrue(split.someSplit());
    }

    @Test
    void testSplitOfExact() {
        DonationPart donationPart = new DonationPart(BigDecimal.valueOf(100), LocalDateTime.now());
        Splittable.Split<DonationPart, BigDecimal> split = donationPart.splitOf(BigDecimal.valueOf(100));

        assertTrue(split.getSplit().isPresent());
        assertEquals(split.getSplit().get().getAmount(), BigDecimal.valueOf(100));
        assertTrue(split.getOpen().isEmpty());
        assertTrue(split.getRemain().isEmpty());
        assertTrue(split.fullSplit());
    }

    @Test
    void testSplitOfMore() {
        DonationPart donationPart = new DonationPart(BigDecimal.valueOf(100), LocalDateTime.now());
        Splittable.Split<DonationPart, BigDecimal> split = donationPart.splitOf(BigDecimal.valueOf(150));

        assertTrue(split.getSplit().isPresent());
        assertEquals(split.getSplit().get().getAmount(), BigDecimal.valueOf(100));
        assertTrue(split.getOpen().isPresent());
        assertEquals(split.getOpen().get(), BigDecimal.valueOf(50));
        assertTrue(split.fullOpenSplit());
    }

    @Test
    void testSplitEmpty() {
        DonationPart donationPart = new DonationPart(BigDecimal.valueOf(0), LocalDateTime.now());
        assertThrows(IllegalStateException.class, () -> donationPart.splitOf(BigDecimal.valueOf(100)));
    }

        */
}
