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
        this.unboundDonations = new DonationQueue(this);
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

    /**
     * Donate to this specific account of an organisation. The donation is time-stamped with the current time and 
     * linked to the donor.
     * 
     * @param donor The donor of the donation.
     * @param amount The amount of the donation.
     */
    public void donate(String donor, BigDecimal amount) {
        Donation donation = new Donation(donor, amount);
        unboundDonations.addAll(donation.getParts());
    }

    /**
     * Donate to this specific account of an organisation. The donation is time-stamped with the current time and
     * linked to the donor. The donation is earmarked for a specific purpose.
     * 
     * @param donor The donor of the donation.
     * @param amount The amount of the donation.
     * @param boundTo The earmarking of the donation.
     * @return True if the donation was successful, false otherwise. A donation can fail if the 
     * earmarking does not exist.
     */
    public boolean donate(String donor, BigDecimal amount, String boundTo) {
        Optional<DonationQueue> boundQueue = getBoundQueue(boundTo);

        if (boundQueue.isPresent()) {
            Donation donation = new Donation(donor, amount);
            boundQueue.get().addAll(donation.getParts());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Spend the given expense from the donations. If the expense is not fully covered by the donations,
     * the remaining amount is added to the negative balance, as long as it is covered by the bound donations.
     * 
     * @param expense The expense to spend from the donations.
     */
    protected boolean spend(Expense expense) {
        // If the total balance is less than the expense, we cannot spend it
        if (less(totalBalance(), expense.getAmount())) {
            return false;
        }
        
        if (expense.isBound()) {
            return spendBound(expense);
        } else {
            return spendUnbound(expense);
        }
    }
    
    private boolean spendUnbound(Expense expense) {
        // 1. Expense is unbound, withdraw from unbound donations
        //    - if unbound are not enough, go into minus as long it is covered by bound donations
        //      (we don't need to subtract form bound, as they are reserved and the organization
        //       must cover the expense from unbound later on)
        // 2. Expense is unbound, but not enough unbound donations, return false
        
        Expense remainingExpense = spendUnboundFromAccount(expense);
        if (remainingExpense.isPaid()) {
            return true;
        }
        
        // Now we need to reserve upstream
        // TODO: Implement this
        return false;
    }
    
    private Expense spendUnboundFromAccount(Expense expense) {
        var polled = this.unboundDonations.getDonationsQueue().pollUnreserved(expense.getAmount());
        polled._1().forEach(expense::payWith);
        return expense;
    }
    
    private boolean spendBound(Expense expense) {
        // 1. Expense is bound, withdraw from bound donations
        //    - if bound are not enough, check if up the queue are more and reserve them, go into minus
        //      as long it is covered by unbound donations
        // 2. Expense is bound, but not enough bound donations up queue, withdraw from unbound donations.
        //    - if unbound are not enough, do not go into minus, as an account must be covered, return false
        String earmarking = expense.getEarmarking();
        
        // TODO: Implement this
        return false;
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

    protected void addEarmarking(String earmarking) {
        boundDonations.add(new Tuple2<>(earmarking, new DonationQueue(this)));
    }

    private Optional<DonationQueue> getBoundQueue(String earmarking) {
        return boundDonations.stream()
                .filter(bound -> bound._1.equals(earmarking))
                .map(Tuple2::_2)
                .findFirst();
    }
    
    DonationQueue getUnboundDonations() {
        return unboundDonations;
    }
    
    List<Tuple2<String, DonationQueue>> getBoundDonations() {
        return boundDonations;
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
        return getBoundQueue(earmarking).map(DonationQueue::totalBalance).orElse(BigDecimal.ZERO);
    }
}
