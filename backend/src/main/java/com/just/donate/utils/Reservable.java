package com.just.donate.utils;

import java.util.Optional;

public class Reservable<R extends Splittable<R, S>, S, C> {

    private final R value;
    private Optional<C> context;

    public Reservable(R value) {
        this.value = value;
        this.context = Optional.empty();
    }

    public boolean isReserved() {
        return context.isPresent();
    }

    public void reserve(C context) {
        this.context = Optional.of(context);
    }

    public void release() {
        this.context = Optional.empty();
    }

    public R getValue() {
        return value;
    }

    public Optional<C> getContext() {
        return context;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
