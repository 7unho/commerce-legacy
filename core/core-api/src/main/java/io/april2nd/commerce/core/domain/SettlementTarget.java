package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SettlementTarget(
        Long merchantId,
        BigDecimal targetAmount,
        LocalDate settlementDate,
        Long targetCount,
        Long orderCount
) {
}
