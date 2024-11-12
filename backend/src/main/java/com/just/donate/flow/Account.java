package com.just.donate.flow;

import io.vavr.Tuple2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.just.donate.utils.Utils.less;

public class Account {

    private final String name;
    private final List<Tuple2<String, DonationQueue>> boundDonations;
    private final DonationQueue unboundDonations;

    private final List<Account> incomingFlow;
    private final List<Account> outgoingFlow;

    public Account(String name) {
        this.name = name;
        this.boundDonations = new ArrayList<>();
        this.unboundDonations = new DonationQueue();
        this.incomingFlow = new ArrayList<>();
        this.outgoingFlow = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addIncomingFlow(Account account) {
        if (account == this) {
            throw new IllegalArgumentException("Account cannot have incoming flow from itself");
        }

        if (incomingFlow.contains(account)) {
            return;
        }

        incomingFlow.add(account);
        account.addOutgoingFlow(this);
    }

    protected void addOutgoingFlow(Account account) {
        if (account == this) {
            throw new IllegalArgumentException("Account cannot have outgoing flow to itself");
        }

        if (outgoingFlow.contains(account)) {
            return;
        }

        outgoingFlow.add(account);
        account.addIncomingFlow(this);
    }

    public void donate(String donor, BigDecimal amount) {
        Donation donation = new Donation(donor, amount);
        unboundDonations.add(donation);
    }

    public boolean donate(String donor, BigDecimal amount, String boundTo) {
        Optional<DonationQueue> boundQueue = boundDonations.stream()
                .filter(bound -> bound._1.equals(boundTo))
                .map(Tuple2::_2)
                .findFirst();

        if (boundQueue.isPresent()) {
            Donation donation = new Donation(donor, amount);
            return boundQueue.get().add(donation);
        } else {
            return false;
        }
    }

    public void addEarmarking(String earmarking) {
        boundDonations.add(new Tuple2<>(earmarking, new DonationQueue()));
    }

    protected boolean spend(Expense expense) {
        // Cases:
        // 1. Expense is unbound, withdraw from unbound donations
        //    - if unbound are not enough, go into minus as long it is covered by bound donations
        //      (we don't need to subtract form bound, as they are reserved and the organization
        //       must cover the expense from unbound later on)
        // 2. Expense is bound, withdraw from bound donations
        //    - if bound are not enough, check if up the queue are more and reserve them, go into minus
        //      as long it is covered by unbound donations
        // 3. Expense is bound, but not enough bound donations up queue, withdraw from unbound donations.
        //    - if unbound are not enough, do not go into minus, as an account must be covered, return false

        if (expense.isBound()) {
            // Can we cover with only expenses from this bound queue?
            if (less(expense.getAmount(), getBoundQueue(expense.getEarmarking()).get().totalBalance())) {
                getBoundQueue(expense.getEarmarking()).get().spendGoMinus(expense);
                return true;
            } else {
                // We need to check if we can cover the expense with donations from upstream accounts
                BigDecimal upstreamBalance = getUpstreamBalance(expense.getEarmarking());
                if (less(expense.getAmount(), upstreamBalance)) {
                    getBoundQueue(expense.getEarmarking()).get().spendGoMinus(expense);
                    return true;
                } else {
                    spendUnbound(expense);
                    return true;
                }
            }
        } else {
            spendUnbound(expense);
            return true;
        }
    }

    /**
     * Spend the given expense from the unbound donations. If the expense is not fully covered by the donations,
     * the remaining amount is added to the negative balance, as long as it is covered by the bound donations.
     * @param expense The expense to spend from the unbound donations.
     */
    private void spendUnbound(Expense expense) {
        if (less(expense.getAmount(), totalBalance())) {
            throw new IllegalStateException("Expense is bigger than total account balance");
        }

        unboundDonations.spendGoMinus(expense);
    }

    private BigDecimal totalBalanceUnbound() {
        return unboundDonations.totalBalance();
    }

    private BigDecimal getUpstreamBalance(String earmarking) {
        BigDecimal balance = getBoundQueue(earmarking).map(DonationQueue::totalBalance).orElse(BigDecimal.ZERO);
        for (Account account : incomingFlow) {
            balance = balance.add(account.getUpstreamBalance(earmarking));
        }
        return balance;
    }

    private Optional<DonationQueue> getBoundQueue(String earmarking) {
        return boundDonations.stream()
                .filter(bound -> bound._1.equals(earmarking))
                .map(Tuple2::_2)
                .findFirst();
    }

    private BigDecimal totalBalanceBound() {
        return boundDonations.stream()
                .map(bound -> bound._2.totalBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected BigDecimal totalBalance() {
        return totalBalanceUnbound().add(totalBalanceBound());
    }

    protected BigDecimal totalEarmarkedBalance(String earmarking) {
        return getBoundQueue(earmarking).get().totalBalance();
    }
}
