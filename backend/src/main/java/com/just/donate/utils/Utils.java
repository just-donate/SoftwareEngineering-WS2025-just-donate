package com.just.donate.utils;

import java.math.BigDecimal;

public class Utils {

    public static boolean less(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) < 0;
    }

    public static boolean lessOrEqual(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0;
    }

    public static boolean greater(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) > 0;
    }

    public static boolean greaterOrEqual(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0;
    }

}
