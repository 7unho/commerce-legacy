package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OwnedCouponRepository extends JpaRepository<OwnedCouponEntity, Long> {
    OwnedCouponEntity findByUserIdAndCouponId(Long userId, Long couponId);

    List<OwnedCouponEntity> findByUserIdAndStatus(Long userId, EntityStatus status);

    @Query(
            value = """
                    SELECT DISTINCT ownedCoupon FROM OwnedCouponEntity ownedCoupon
                        JOIN CouponEntity coupon
                            ON ownedCoupon.couponId = coupon.id
                            AND ownedCoupon.userId = :userId
                            AND ownedCoupon.state = 'DOWNLOADED'
                            AND ownedCoupon.status = 'ACTIVE'
                    WHERE 
                        coupon.id IN :couponIds
                        AND coupon.status = 'ACTIVE'
                        AND coupon.expiredAt > :expiredAtAfter            
                    """
    )
    List<OwnedCouponEntity> findOwnedCouponIds(Long userId, Collection<Long> couponIds, LocalDateTime expiredAtAfter);
}
