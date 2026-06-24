package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;

public record SettlementTransferTarget(
        Long settlementId,
        Long merchantId,
        BigDecimal settlementAmount
) {
}
