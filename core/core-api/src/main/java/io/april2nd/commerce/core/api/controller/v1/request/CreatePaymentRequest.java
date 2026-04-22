package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.OwnedCoupon;
import io.april2nd.commerce.core.domain.PaymentDiscount;
import io.april2nd.commerce.core.domain.PointBalance;

import java.math.BigDecimal;
import java.util.List;

public record CreatePaymentRequest(
        String orderKey,
        Long useOwnedCouponId,
        BigDecimal usePoint
) {
    public PaymentDiscount toPaymentDiscount(List<OwnedCoupon> ownedCoupons, PointBalance pointBalance) {
        return new PaymentDiscount(
                ownedCoupons,
                pointBalance,
                useOwnedCouponId != null ? useOwnedCouponId : -1,
                usePoint != null ? usePoint : BigDecimal.valueOf(-1)
        );
    }
}
