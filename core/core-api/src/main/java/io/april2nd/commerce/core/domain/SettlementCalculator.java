package io.april2nd.commerce.core.domain;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class SettlementCalculator {
    private static final BigDecimal DEFAULT_FEE_RATE = BigDecimal.valueOf(0.1);
    private static final BigDecimal TEN_MILLION_FEE_RATE = BigDecimal.valueOf(0.08);
    private static final BigDecimal HUNDRED_MILLION_FEE_RATE = BigDecimal.valueOf(0.05);
    private static final BigDecimal TEN_MILLION = BigDecimal.valueOf(10_000_000);
    private static final BigDecimal HUNDRED_MILLION = BigDecimal.valueOf(100_000_000);

    public static SettlementAmount calculate(BigDecimal amount, BigDecimal recentSalesAmount) {
        BigDecimal feeRate = feeRate(recentSalesAmount);
        BigDecimal feeAmount = amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal settlementAmount = amount.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);

        return new SettlementAmount(amount, feeAmount, feeRate, settlementAmount);
    }

    private static BigDecimal feeRate(BigDecimal recentSalesAmount) {
        if (recentSalesAmount.compareTo(HUNDRED_MILLION) > 0) {
            return HUNDRED_MILLION_FEE_RATE;
        }

        if (recentSalesAmount.compareTo(TEN_MILLION) > 0) {
            return TEN_MILLION_FEE_RATE;
        }

        return DEFAULT_FEE_RATE;
    }
}
