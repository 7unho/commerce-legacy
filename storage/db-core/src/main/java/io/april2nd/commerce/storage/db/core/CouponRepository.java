package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<CouponEntity, Long> {
    List<CouponEntity> findByIdInAndStatus(Collection<Long> ids, EntityStatus status);

    Optional<CouponEntity> findByIdAndStatusAndExpiredAtAfter(Long couponId, EntityStatus status, LocalDateTime expiredAtAfter);

    @Query(
            value = """
                     SELECT DISTINCT coupon FROM CouponEntity coupon
                        LEFT JOIN CouponTargetEntity productTarget
                            ON productTarget.couponId = coupon.id
                            AND productTarget.targetType = 'PRODUCT'
                            AND productTarget.targetId IN :productIds
                            AND productTarget.status = 'ACTIVE'
                        LEFT JOIN CouponTargetEntity categoryTarget
                            ON categoryTarget.couponId = coupon.id
                            AND categoryTarget.targetType = 'PRODUCT_CATEGORY'
                            AND categoryTarget.status = 'ACTIVE'
                        WHERE coupon.status = 'ACTIVE'
                          AND (
                              productTarget.id IS NOT NULL
                              OR categoryTarget.targetId IN (
                                  SELECT productCategory.categoryId
                                  FROM ProductCategoryEntity productCategory
                                  WHERE productCategory.productId IN :productIds
                                    AND productCategory.status = 'ACTIVE'
                              )
                          )
                    """
    )
    List<CouponEntity> findApplicableCouponIds(Collection<Long> productIds);
}
