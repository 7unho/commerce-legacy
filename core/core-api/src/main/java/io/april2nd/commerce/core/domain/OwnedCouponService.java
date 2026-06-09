package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
public class OwnedCouponService {
    private final CouponDownloader couponDownloader;
    private final OwnedCouponReader ownedCouponReader;
    private final CouponTargetReader couponTargetReader;

    public void download(User user, Long couponId) {
        couponDownloader.download(user.id(), couponId);
    }

    public List<OwnedCoupon> getOwnedCoupons(User user) {
        return ownedCouponReader.getOwnedCoupons(user.id());
    }

    public List<OwnedCoupon> getOwnedCouponsForCheckout(User user, Collection<Long> productIds) {
        if (productIds.isEmpty()) return emptyList();
        Set<Long> applicableCouponIds = couponTargetReader.findCouponIdsByProductIds(productIds);
        if (applicableCouponIds.isEmpty()) return emptyList();

        return ownedCouponReader.findOwnedForCheckout(user.id(), applicableCouponIds, LocalDateTime.now());
    }
}
