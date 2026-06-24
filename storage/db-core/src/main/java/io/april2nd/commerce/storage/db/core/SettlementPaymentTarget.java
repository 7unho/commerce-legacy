package io.april2nd.commerce.storage.db.core;

public record SettlementPaymentTarget(
        Long orderId,
        Long paymentId
) {
}
