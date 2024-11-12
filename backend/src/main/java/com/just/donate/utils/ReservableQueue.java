package com.just.donate.utils;

import io.vavr.Tuple2;
import io.vavr.collection.List;

public class ReservableQueue<T extends Splittable<T, S>, S, C> {

    private List<Reservable<T, S, C>> queue = List.empty();

    public ReservableQueue() {

    }


    public void add(T value) {
        queue = queue.append(new Reservable<>(value));
    }

    public void addValues(List<T> values) {
        queue = queue.appendAll(values.map(Reservable::new));
    }

    public void addAll(List<Reservable<T, S, C>> values, C addedTo) {
        values = values.map(r -> {
            if (r.isReservedBy(addedTo)) {
                r.release();
            }
            return r;
        });
        queue = queue.appendAll(values);
    }

    public S reserve(S s, C context) {
        Tuple2<S, List<Reservable<T, S, C>>> res = reserve(s, queue, context);
        queue = res._2;
        return res._1;
    }

    private Tuple2<S, List<Reservable<T, S, C>>> reserve(S s, List<Reservable<T, S, C>> values, C context) {
        // If there are no values left, return the original split value
        if (values.isEmpty()) {
            return new Tuple2<>(s, values);
        }

        Reservable<T, S, C> head = values.head();

        if (head.isReserved()) {
            return reserve(s, values.tail(), context).map2(tail -> tail.prepend(head));
        }

        Splittable.Split<T, S> split = head.getValue().splitOf(s);

        // s = 0, then we do not split anything of, split is empty, remain is the original value and open is empty
        if (split.fullRemain()) {
            return new Tuple2<>(s, values.tail().prepend(head));
        }

        // s < value, then we split off s, remain is the remaining value and open is empty
        if (split.someSplit()) {
            // Reserve the part that is split off
            Reservable<T, S, C> splitOf = new Reservable<>(split.getSplit().get());
            splitOf.reserve(context);
            return new Tuple2<>(null, values.tail().prepend(splitOf).prepend(new Reservable<>(split.getRemain().get())));
        }

        // s = value, then we split the whole value off, remain is empty and open is empty
        if (split.fullSplit()) {
            Reservable<T, S, C> splitOf = new Reservable<>(split.getSplit().get());
            splitOf.reserve(context);
            return new Tuple2<>(null, values.tail().prepend(splitOf));
        }

        // s > value, then we split the whole value off, remain is empty and open is the remaining value
        if (split.fullOpenSplit()) {
            Reservable<T, S, C> splitOf = new Reservable<>(split.getSplit().get());
            splitOf.reserve(context);
            return reserve(split.getOpen().get(), values.tail(), context).map2(tail -> tail.prepend(splitOf));
        }

        throw new IllegalStateException("Should not happen?");
    }


}
