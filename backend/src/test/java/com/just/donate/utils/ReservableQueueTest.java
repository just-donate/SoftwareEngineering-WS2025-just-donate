package com.just.donate.utils;

import org.junit.jupiter.api.Test;

public class ReservableQueueTest {
    
    private static class TestSplittable implements Splittable<TestSplittable, Integer> {
        
        
        
        @Override
        public Split<TestSplittable, Integer> splitOf(Integer integer) {
            return null;
        }
    }
    
    

    
}
