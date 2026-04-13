package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<CouponEntity, Long> {
    List<CouponEntity> findByIdInAndStatus(Collection<Long> ids, EntityStatus status);

    Optional<CouponEntity> findByIdAndStatusAndExpiredAtAfter(Long couponId, EntityStatus status, LocalDateTime expiredAtAfter);
}
