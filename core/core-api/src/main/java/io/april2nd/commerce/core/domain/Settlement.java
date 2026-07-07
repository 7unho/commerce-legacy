package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SettlementState;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Settlement(
        Long id,
        Long merchantId,
        LocalDate settlementDate,
        BigDecimal originalAmount,
        BigDecimal feeAmount,
        BigDecimal feeRate,
        BigDecimal settlementAmount,
        SettlementState state
) {
}
