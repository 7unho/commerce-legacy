package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CouponTargetType;
import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CouponTargetRepository extends JpaRepository<CouponTargetEntity, Long> {
    List<CouponTargetEntity> findByTargetTypeAndTargetIdInAndStatus(CouponTargetType couponTargetType, Collection<Long> productIds, EntityStatus entityStatus);
}
