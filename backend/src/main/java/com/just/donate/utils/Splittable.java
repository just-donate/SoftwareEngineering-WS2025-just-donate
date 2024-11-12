package com.just.donate.utils;

import java.util.Optional;

/**
 * Interface for objects that can be split up into two parts by a split value. The split value is used to determine
 * how much of the object is split off and how much remains. Optionally nothing remains and some split value remains 
 * open. 
 * @param <T> The type of the object that can be split.
 * @param <S> The type of the object that determines the split, and which can be open if it's not covered.
 */
public interface Splittable<T, S> {

    public static class Split<T, S> {
        private final Optional<T> split;
        private final Optional<T> remain;
        private final Optional<S> open;

        public Split(Optional<T> split, Optional<T> remain, Optional<S> open) {
            this.split = split;
            this.remain = remain;
            this.open = open;
        }

        public Optional<T> getSplit() {
            return this.split;
        }

        public Optional<T> getRemain() {
            return this.remain;
        }

        public Optional<S> getOpen() {
            return this.open;
        }
    }

    public Split<T, S> splitOf(S s);

}
