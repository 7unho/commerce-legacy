package io.april2nd.commerce.core.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SettlementCalculator {

    private static final BigDecimal FEE = BigDecimal.valueOf(0.1);

    private SettlementCalculator() {
        // Utility class
    }

    public static SettlementAmount calculate(BigDecimal amount) {
        BigDecimal feeAmount = amount.multiply(FEE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal settlementAmount = amount.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);

        return new SettlementAmount(amount, feeAmount, FEE, settlementAmount);
    }
}
