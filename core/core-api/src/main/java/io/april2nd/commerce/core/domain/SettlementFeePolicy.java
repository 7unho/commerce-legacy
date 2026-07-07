package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;
import java.util.Arrays;

public enum SettlementFeePolicy {
    OVER_100M(100_000_000L, BigDecimal.valueOf(0.05)),
    OVER_10M(10_000_000L, BigDecimal.valueOf(0.08)),
    DEFAULT(0L, BigDecimal.valueOf(0.1));

    private final BigDecimal threshold;
    private final BigDecimal feeRate;

    SettlementFeePolicy(long threshold, BigDecimal feeRate) {
        this.threshold = BigDecimal.valueOf(threshold);
        this.feeRate = feeRate;
    }

    public static BigDecimal feeRate(BigDecimal recentSalesAmount) {
        return Arrays.stream(values())
                .filter(policy -> recentSalesAmount.compareTo(policy.threshold) > 0)
                .findFirst()
                .orElse(DEFAULT)
                .feeRate;
    }
}
