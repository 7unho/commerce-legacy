package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;

public record SettlementAmount(
        BigDecimal originalAmount,
        BigDecimal feeAmount,
        BigDecimal feeRate,
        BigDecimal settlementAmount
) {
}
