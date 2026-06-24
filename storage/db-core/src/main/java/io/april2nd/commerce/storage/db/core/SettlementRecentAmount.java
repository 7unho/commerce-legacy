package io.april2nd.commerce.storage.db.core;

import java.math.BigDecimal;

public record SettlementRecentAmount(
        Long merchantId,
        BigDecimal amount
) {
}
