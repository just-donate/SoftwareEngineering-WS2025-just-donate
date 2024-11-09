package com.just.donate.flow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Account {
    private final String name;

    private final List<Donation> donations;
    private final List<Expense> expenses;

    private final List<BoundDonationQueue> boundDonationQueues;
    private final UnboundDonationQueue unboundDonations;

    private final List<Account> incomingAccounts;
    private final List<Account> outgoingAccounts;

    public Account(String name) {
        this.name = name;
        this.donations = new ArrayList<>();
        this.expenses = new ArrayList<>();
        this.boundDonationQueues = new ArrayList<>();
        this.unboundDonations = new UnboundDonationQueue();
        this.incomingAccounts = new ArrayList<>();
        this.outgoingAccounts = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    private Optional<DonationQueue> getDonationQueue(DonationType donationType) {
        switch (donationType) {
            case UnboundedDonationType unboundedDonationType -> {
                return Optional.of(this.unboundDonations);
            }
            case BoundedDonationType boundedDonationType -> {
                return this.boundDonationQueues
                        .stream()
                        .filter(d -> d.getBucketName() == boundedDonationType.boundName())
                        .findFirst()
                        .map(d -> (DonationQueue) d);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void addIncomingConnection(Account account) {
        this.incomingAccounts.add(account);
        account.outgoingAccounts.add(this);
    }

    public void addDonation(Donation donation, DonationType donationType) {
        this.donations.add(donation);
        this.addDonationPart(DonationPart.fromDonation(donation), donationType);
    }

    private void addDonationPart(DonationPart donationPart, DonationType donationType) {
        var queue = this.getDonationQueue(donationType).get();

        if (queue.getTotalPending().signum() == 1) {
            BigDecimal amountToAdd;

            if (queue.getTotalPending().compareTo(donationPart.getAmount()) == -1) {
                amountToAdd = queue.getTotalPending();
                donationPart.setAmount(donationPart.getAmount().subtract(amountToAdd));
                DonationPart usedDonationPart = new DonationPart(donationPart.getDonation(), amountToAdd);
                // TODO: notify expense and usedDonationPart - at this point i don't have the
                // expense reference
                // usedDonationPart.setExpense(expense);
                queue.setTotalPending(queue.getTotalPending().subtract(amountToAdd));
            } else {
                amountToAdd = donationPart.getAmount();
                // TODO: notify expense and donationPart - at this point i don't have the
                // expense reference
                // donationPart.setExpense(expense);
                queue.setTotalPending(queue.getTotalPending().subtract(amountToAdd));
                return;
            }
        }

        queue.addDonation(donationPart);
    }

    public void printQueues() {
        for (BoundDonationQueue queue : this.boundDonationQueues) {
            var res = String.format("== BoundQueue: %s (pending: %0.00d)", queue.getBucketName(),
                    queue.getTotalPending());
            System.out.println(res);
            queue.print();
        }

        var res = String.format("== UnboundQueue: (pending: %.2f)", this.unboundDonations.getTotalPending());
        System.out.println(res);
        this.unboundDonations.print();
    }

    public void makeTransaction(BigDecimal amount, Account account, DonationType donationType) {
        var queue = this.getDonationQueue(donationType).get();

        BigDecimal remainingTotal = amount;

        Iterator<DonationPart> iter = queue.stream().filter(d -> d.getDestinationMark().isEmpty())
                .iterator();

        while (remainingTotal.signum() == 1) {
            if (!iter.hasNext()) {
                // TODO: search up the tree for unmarked donation parts

                queue.setTotalPending(queue.getTotalPending().add(remainingTotal));
                return;
            }

            DonationPart current = iter.next();
            BigDecimal amountToTransfer;

            if (remainingTotal.compareTo(current.getAmount()) == -1) {
                amountToTransfer = remainingTotal;
                current.setAmount(current.getAmount().subtract(amountToTransfer));
                current = new DonationPart(
                        current.getDonation(),
                        amountToTransfer);
            } else {
                queue.remove(current);
                amountToTransfer = current.getAmount();
            }

            account.addDonationPart(current, donationType);
            remainingTotal = remainingTotal.subtract(amountToTransfer);
        }
    }

    public void addExpense(Expense expense, DonationType donationType) {
        var queue = this.getDonationQueue(donationType).get();

        this.expenses.add(expense);

        Iterator<DonationPart> iter = queue.stream().filter(d -> d.getDestinationMark().isEmpty())
                .iterator();

        BigDecimal remainingTotal = expense.getAmount();

        while (remainingTotal.signum() == 1) {
            if (!iter.hasNext()) {
                // TODO: search up the tree + mark donations

                queue.setTotalPending(queue.getTotalPending().add(remainingTotal));
                return;
            }

            DonationPart donationPart = iter.next();
            BigDecimal amountToTransfer;

            if (remainingTotal.compareTo(donationPart.getAmount()) == -1) {
                amountToTransfer = remainingTotal;
                donationPart.setAmount(donationPart.getAmount().subtract(amountToTransfer));
                donationPart = new DonationPart(
                        donationPart.getDonation(),
                        amountToTransfer);
            } else {
                queue.remove(donationPart);
                amountToTransfer = donationPart.getAmount();
            }

            donationPart.setExpense(expense);
            remainingTotal = remainingTotal.subtract(amountToTransfer);
        }
    }
}
