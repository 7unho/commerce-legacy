package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CancelType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementCancel(
        Long id,
        Long userId,
        CancelType type,
        Long orderId,
        Long orderItemId,
        Long paymentId,
        BigDecimal originAmount,
        Long ownedCouponId,
        BigDecimal couponDiscount,
        BigDecimal usedPoint,
        BigDecimal paidAmount,
        Long canceledQuantity,
        BigDecimal canceledPaidAmount,
        BigDecimal canceledPointAmount,
        BigDecimal canceledCouponAmount,
        String externalCancelKey,
        LocalDateTime canceledAt
) {
}
