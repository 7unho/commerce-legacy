package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PaymentMethod;
import io.april2nd.commerce.core.enums.PaymentState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementPayment(
        Long id,
        Long userId,
        Long orderId,
        BigDecimal originAmount,
        Long ownedCouponId,
        BigDecimal couponDiscount,
        BigDecimal usedPoint,
        Long payerUserId,
        BigDecimal paidAmount,
        PaymentState state,
        String externalPaymentKey,
        PaymentMethod method,
        String approveCode,
        LocalDateTime paidAt
) {}
