package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.TransactionType;

import java.math.BigDecimal;

public record SettlementTargetItem(
        TransactionType transactionType,
        Long transactionId,
        Long orderId,
        Long productId,
        Long quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
