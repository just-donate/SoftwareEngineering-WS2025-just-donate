package com.just.donate.utils;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;

public class ReservableQueue<T extends Splittable<T, S>, S, C> {

    private List<Reservable<T, S, C>> queue = List.empty();
    private final C context;

    public ReservableQueue(C context) {
        this.context = context;
    }

    public void add(T value) {
        queue = queue.append(new Reservable<>(value));
    }

    public void add(Reservable<T, S, C> value) {
        if (value.isReservedBy(this.context)) {
            value.release();
        }
        queue = queue.append(value);
    }

    public List<Reservable<T, S, C>> getQueue() {
        return queue;
    }

    public Option<T> pollUnreserved() {
        Reservable<T, S, C> head = queue.find(r -> !r.isReserved()).getOrNull();
        if (head == null) {
            return Option.none();
        } else {
            queue = queue.remove(head);
            return Option.some(head.getValue());
        }
    }

    /**
     * Poll amount s from unreserved Reservables from the queue, split with s if necessary.
     *
     * @param s The amount to poll
     * @return The polled value and the remaining amount s which is not covered by the polled values.
     */
    public Tuple2<List<T>, S> pollUnreserved(S s) {
        Option<Reservable<T, S, C>> peeked = queue.find(r -> !r.isReserved());

        // If there are no unreserved values, return the original split value
        if (peeked.isEmpty()) {
            return new Tuple2<>(List.empty(), s);
        }

        T head = peeked.get().getValue();
        Splittable.Split<T, S> split = head.splitOf(s);

        // s = 0, nothing was split of, s must be 0, we just return it
        if (split.fullRemain()) {
            return new Tuple2<>(List.of(head), s);
        }

        // s < value, then we split off s, remain is the remaining value and open is empty
        if (split.someSplit()) {
            queue = queue.replace(peeked.get(), new Reservable<>(split.getRemain().get()));
            return new Tuple2<>(List.of(split.getSplit().get()), s);
        }

        // s = value, then we split the whole value off, remain is empty and open is empty
        if (split.fullSplit()) {
            queue = queue.remove(peeked.get());
            return new Tuple2<>(List.of(split.getSplit().get()), s);
        }

        // s > value, then we split the whole value off, remain is empty and open is the remaining value
        if (split.fullOpenSplit()) {
            queue = queue.remove(peeked.get());
            Tuple2<List<T>, S> polled = pollUnreserved(split.getOpen().get());
            return new Tuple2<>(polled._1.prepend(split.getSplit().get()), polled._2);
        }

        throw new IllegalStateException("Should not happen?");
    }

    public Option<T> peekUnreserved() {
        return queue.find(r -> !r.isReserved()).map(Reservable::getValue);
    }

    public boolean hasUnreserved() {
        return queue.exists(r -> !r.isReserved());
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isFullyReserved() {
        return queue.forAll(Reservable::isReserved);
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
            return new Tuple2<>(null, values.tail().prepend(new Reservable<>(split.getRemain().get())).prepend(splitOf));
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

    @Override
    public String toString() {
        return "[" + queue.map(Reservable::toString).zipWithIndex()
                .map(t -> String.format("%d: %s", t._2 + 1, t._1)).mkString(", ") + "]";
    }


}
