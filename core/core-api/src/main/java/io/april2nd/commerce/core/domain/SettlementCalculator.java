package io.april2nd.commerce.core.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SettlementCalculator {
    public SettlementAmount calculate(BigDecimal amount, BigDecimal recentSalesAmount) {
        BigDecimal feeRate = SettlementFeePolicy.feeRate(recentSalesAmount);
        BigDecimal feeAmount = amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal settlementAmount = amount.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);

        return new SettlementAmount(amount, feeAmount, feeRate, settlementAmount);
    }
}
