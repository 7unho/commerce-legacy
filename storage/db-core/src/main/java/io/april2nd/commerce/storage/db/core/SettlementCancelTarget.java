package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CancelType;

public record SettlementCancelTarget(
        Long cancelId,
        CancelType cancelType,
        Long orderId,
        Long orderItemId,
        Long canceledQuantity
) {
}
