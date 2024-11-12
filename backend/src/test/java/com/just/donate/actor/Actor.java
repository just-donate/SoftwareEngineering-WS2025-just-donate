package com.just.donate.actor;

public abstract class Actor {

    private final String name;

    public Actor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
