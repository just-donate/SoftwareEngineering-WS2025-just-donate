package com.just.donate.utils;

import java.util.Optional;

public interface Splittable<T, S> {

    public static class Split<T> {
        private final Optional<T> splitOf;
        private final Optional<T> remaining;

        private Split(Optional<T> splitOf, Optional<T> remaining) {
            this.splitOf = splitOf;
            this.remaining = remaining;
        }

        public Optional<T> getSplitOf() {
            return this.splitOf;
        }

        public Optional<T> getRemaining() {
            return this.remaining;
        }

        public static <T> Split<T> noRemaining(T splitOff) {
            return new Split<>(Optional.of(splitOff), Optional.empty());
        }

        public static <T> Split<T> withRemaining(T splitOff, T remaining) {
            return new Split<>(Optional.of(splitOff), Optional.of(remaining));
        }
    }

    public Split<T> splitOf(S s);

    public boolean canSplit(S s);

}
