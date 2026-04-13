package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.OwnedCoupon;
import io.april2nd.commerce.core.enums.CouponType;
import io.april2nd.commerce.core.enums.OwnedCouponState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OwnedCouponResponse(
        Long id,
        OwnedCouponState state,
        String name,
        CouponType type,
        BigDecimal discount,
        LocalDateTime expiredAt
) {
    public static OwnedCouponResponse of(OwnedCoupon ownedCoupon) {
        return new OwnedCouponResponse(
                ownedCoupon.id(),
                ownedCoupon.state(),
                ownedCoupon.coupon().name(),
                ownedCoupon.coupon().type(),
                ownedCoupon.coupon().discount(),
                ownedCoupon.coupon().expiredAt()
        );
    }
    public static List<OwnedCouponResponse> of(List<OwnedCoupon> coupons) {
        return coupons.stream()
                .map(OwnedCouponResponse::of)
                .collect(Collectors.toList());
    }
}
