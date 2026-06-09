package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.OwnedCouponEntity;
import io.april2nd.commerce.storage.db.core.error.IllegalCouponUsageException;
import io.april2nd.commerce.storage.db.core.OwnedCouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OwnedCouponUsageManager {
    private final OwnedCouponRepository ownedCouponRepository;
    private static final Logger log = LoggerFactory.getLogger(OwnedCouponUsageManager.class);

    @Transactional
    public void use(Long ownedCouponId) {
        try {
            OwnedCouponEntity ownedCoupon = ownedCouponRepository.findById(ownedCouponId)
                    .orElseThrow(() -> new CoreException(ErrorType.OWNED_COUPON_INVALID));
            ownedCoupon.use();
        } catch (IllegalCouponUsageException e) {
            log.error("[OWNED_COUPON_USAGE] 비정상적인 쿠폰 사용 - 사용 완료 쿠폰 ownedCouponId: {}", ownedCouponId, e);
            throw new CoreException(ErrorType.OWNED_COUPON_INVALID_USAGE);
        } catch (OptimisticLockingFailureException e) {
            log.error("[OWNED_COUPON_USAGE] 비정상적인 쿠폰 사용 - 사용 동시성 충돌 ownedCouponId={}", ownedCouponId, e);
            throw new CoreException(ErrorType.OWNED_COUPON_INVALID_USAGE);
        }
    }

    @Transactional
    public void revert(Long ownedCouponId) {
        try {
            OwnedCouponEntity ownedCoupon = ownedCouponRepository.findById(ownedCouponId)
                    .orElseThrow(() -> new CoreException(ErrorType.OWNED_COUPON_INVALID));
            ownedCoupon.revert();
        } catch (IllegalCouponUsageException e) {
            log.error("[OWNED_COUPON_USAGE] 비정상적인 쿠폰 복원- 미사용 쿠폰 복원 시도 ownedCouponId: {}", ownedCouponId, e);
            throw new CoreException(ErrorType.OWNED_COUPON_INVALID_USAGE);
        } catch (OptimisticLockingFailureException e) {
            log.error("[OWNED_COUPON_USAGE] 비정상적인 쿠폰 사용 - 복원 동시성 충돌 ownedCouponId={}", ownedCouponId, e);
            throw new CoreException(ErrorType.OWNED_COUPON_INVALID_USAGE);
        }
    }
}
