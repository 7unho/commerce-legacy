package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponReader couponReader;
    private final CouponTargetReader couponTargetReader;

    public List<Coupon> getCouponsForProducts(Collection<Long> productIds) {
        Set<Long> couponIds = couponTargetReader.findCouponIdsByProductIds(productIds);
        return couponReader.findActiveByIds(couponIds);
    }
}
