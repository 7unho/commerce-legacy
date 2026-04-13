package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.Coupon;
import io.april2nd.commerce.core.enums.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String name,
        CouponType type,
        BigDecimal discount,
        LocalDateTime expiredAt
) {
    static CouponResponse of(Coupon coupon) {
        return new CouponResponse(
                coupon.id(),
                coupon.name(),
                coupon.type(),
                coupon.discount(),
                coupon.expiredAt()
        );
    }
}
