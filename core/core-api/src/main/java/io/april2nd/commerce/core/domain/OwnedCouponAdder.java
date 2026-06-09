package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OwnedCouponState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.OwnedCouponEntity;
import io.april2nd.commerce.storage.db.core.OwnedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OwnedCouponAdder {
    private final OwnedCouponRepository ownedCouponRepository;

    public void addIfNotExists(Long userId, Long couponId, Long maxUseCount) {
        OwnedCouponEntity existing = ownedCouponRepository.findByUserIdAndCouponId(userId, couponId);

        if (existing != null) throw new CoreException(ErrorType.COUPON_ALREADY_DOWNLOADED);

        try {
            ownedCouponRepository.save(
                    new OwnedCouponEntity(
                            userId,
                            couponId,
                            OwnedCouponState.DOWNLOADED,
                            maxUseCount,
                            0L)
            );
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.COUPON_ALREADY_DOWNLOADED);
        }
    }
}
