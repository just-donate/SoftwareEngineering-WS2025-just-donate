package com.just.donate.utils;

import io.vavr.collection.List;

public class ReservableQueue<T extends Splittable<T, S>, S, C> {

    private List<Reservable<T, S, C>> queue = List.empty();

    public void add(T value) {
        queue = queue.append(new Reservable<>(value));
    }

    public void addValues(List<T> values) {
        queue = queue.appendAll(values.map(Reservable::new));
    }

    public void addAll(List<Reservable<T, S, C>> values, C addedTo) {
        return;
    }

    public S reserve(S s) {
        // TODO
        return null;
    }




}
