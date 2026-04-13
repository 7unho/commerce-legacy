package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnedCouponRepository extends JpaRepository<OwnedCouponEntity, Long> {
    OwnedCouponEntity findByUserIdAndCouponId(Long userId, Long couponId);

    List<OwnedCouponEntity> findByUserIdAndStatus(Long userId, EntityStatus status);
}
