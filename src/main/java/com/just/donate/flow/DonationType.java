package com.just.donate.flow;

public sealed interface DonationType permits UnboundedDonationType, BoundedDonationType {
}
