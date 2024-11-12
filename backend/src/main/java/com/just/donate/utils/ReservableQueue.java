package com.just.donate.utils;

import java.util.ArrayList;
import java.util.List;

public class ReservableQueue<T extends Splittable<T, S>, S, C> {

    private int size = 0;
    private List<Reservable<T, S, C>> queue = new ArrayList<>();

    public void add(T value) {
        queue.add(new Reservable<>(value));
        size++;
    }

    public T pollUnreserved() {
        Reservable<T, S, C> polled = null;
        for (Reservable<T, S, C> reservable : queue) {
            if (!reservable.isReserved()) {
                polled = reservable;
                break;
            }
        }
        if (polled != null) {
            queue.remove(polled);
            size--;
            return polled.getValue();
        } else {
            return null;
        }
    }

    public T pollUnreserved(S split) {}




}
