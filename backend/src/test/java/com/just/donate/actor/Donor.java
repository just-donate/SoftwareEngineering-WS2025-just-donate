package com.just.donate.actor;

/**
 * A donor can donate money to an organization.
 * - They can donate to any account of the organization.
 * - They can donate to an earmarking. If they do so the money is only spend on expenses for that earmarking.
 * - They can donate to general causes, in which case the money can be spent on anything.
 */
public class Donor extends Actor {


    public Donor(String name) {
        super(name);
    }

}