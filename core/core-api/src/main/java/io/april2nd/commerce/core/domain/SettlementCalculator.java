package io.april2nd.commerce.core.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SettlementCalculator {
    private static final BigDecimal DEFAULT_FEE_RATE = BigDecimal.valueOf(0.1);
    private static final BigDecimal OVER_10M_FEE_RATE = BigDecimal.valueOf(0.08);
    private static final BigDecimal OVER_100M_FEE_RATE = BigDecimal.valueOf(0.05);

    private static final BigDecimal SALES_THRESHOLD_100M = BigDecimal.valueOf(100_000_000);
    private static final BigDecimal SALES_THRESHOLD_10M = BigDecimal.valueOf(10_000_000);

    public SettlementAmount calculate(BigDecimal amount, BigDecimal recentSalesAmount) {
        BigDecimal feeRate = feeRate(recentSalesAmount);
        BigDecimal feeAmount = amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal settlementAmount = amount.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);

        return new SettlementAmount(amount, feeAmount, feeRate, settlementAmount);
    }

    private static BigDecimal feeRate(BigDecimal recentSalesAmount) {
        if (recentSalesAmount.compareTo(SALES_THRESHOLD_100M) > 0) {
            return OVER_100M_FEE_RATE;
        }

        if (recentSalesAmount.compareTo(SALES_THRESHOLD_10M) > 0) {
            return OVER_10M_FEE_RATE;
        }

        return DEFAULT_FEE_RATE;
    }
}
