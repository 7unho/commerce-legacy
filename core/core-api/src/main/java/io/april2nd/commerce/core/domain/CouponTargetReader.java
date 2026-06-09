package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CouponTargetType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.CouponTargetEntity;
import io.april2nd.commerce.storage.db.core.CouponTargetRepository;
import io.april2nd.commerce.storage.db.core.ProductCategoryEntity;
import io.april2nd.commerce.storage.db.core.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CouponTargetReader {
    private final CouponTargetRepository couponTargetRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public Set<Long> findCouponIdsByProductIds(Collection<Long> productIds) {
        if (productIds.isEmpty()) return Collections.emptySet();

        List<CouponTargetEntity> productTargets = couponTargetRepository.findByTargetTypeAndTargetIdInAndStatus(
                CouponTargetType.PRODUCT,
                productIds,
                EntityStatus.ACTIVE
        );

        List<Long> categoryIds = productCategoryRepository
                .findByProductIdInAndStatus(productIds, EntityStatus.ACTIVE)
                .stream()
                .map(ProductCategoryEntity::getCategoryId)
                .collect(Collectors.toList());

        Stream<CouponTargetEntity> categoryTargetStream = categoryIds.isEmpty()
                ? Stream.empty()
                : couponTargetRepository.findByTargetTypeAndTargetIdInAndStatus(
                CouponTargetType.PRODUCT_CATEGORY,
                categoryIds,
                EntityStatus.ACTIVE
        ).stream();

        return Stream.concat(productTargets.stream(), categoryTargetStream)
                .map(CouponTargetEntity::getCouponId)
                .collect(Collectors.toSet());
    }
}
