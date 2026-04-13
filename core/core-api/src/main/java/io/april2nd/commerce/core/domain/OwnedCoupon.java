package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OwnedCouponState;

public record OwnedCoupon(
        Long id,
        Long userId,
        OwnedCouponState state,
        Coupon coupon
) {}
