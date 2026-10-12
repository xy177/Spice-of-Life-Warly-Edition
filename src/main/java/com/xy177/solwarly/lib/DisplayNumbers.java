package com.xy177.solwarly.lib;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Display only: saved contributions and reward comparisons retain full precision. */
public final class DisplayNumbers {
    public static String format(double value) {
        if (!Double.isFinite(value)) return "0";
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
    private DisplayNumbers() {}
}
