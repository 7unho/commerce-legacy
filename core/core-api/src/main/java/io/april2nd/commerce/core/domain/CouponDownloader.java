package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CouponEntity;
import io.april2nd.commerce.storage.db.core.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CouponDownloader {
    private final CouponRepository couponRepository;
    private final OwnedCouponAdder ownedCouponAdder;

    public void download(Long userId, Long couponId) {
        CouponEntity coupon = couponRepository.findByIdAndStatusAndExpiredAtAfter(
                couponId,
                EntityStatus.ACTIVE,
                LocalDateTime.now()
        ).orElseThrow(() -> new CoreException(ErrorType.COUPON_NOT_FOUND_OR_EXPIRED));

        ownedCouponAdder.addIfNotExists(userId, coupon.getId(), coupon.getMaxUseCount());
    }
}
