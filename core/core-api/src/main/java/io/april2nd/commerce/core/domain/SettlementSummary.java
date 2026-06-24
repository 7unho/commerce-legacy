package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SettlementSummary(
        Long merchantId,
        LocalDate settlementDate,
        BigDecimal targetAmount
) {
}
