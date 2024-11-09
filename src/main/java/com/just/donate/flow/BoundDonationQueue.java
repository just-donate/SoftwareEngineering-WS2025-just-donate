package com.just.donate.flow;

public class BoundDonationQueue extends DonationQueue {
    private final String bucketName;

    public String getBucketName() {
        return bucketName;
    }

    public BoundDonationQueue(String bucketName) {
        this.bucketName = bucketName;
    }
}
