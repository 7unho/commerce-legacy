package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CouponTargetType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.CouponRepository;
import io.april2nd.commerce.storage.db.core.CouponTargetEntity;
import io.april2nd.commerce.storage.db.core.CouponTargetRepository;
import io.april2nd.commerce.storage.db.core.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CouponService {
    private CouponRepository couponRepository;
    private CouponTargetRepository couponTargetRepository;
    private ProductCategoryRepository productCategoryRepository;

    public List<Coupon> getCouponsForProducts(Collection<Long> productIds) {
        List<CouponTargetEntity> productTargets = couponTargetRepository.findByTargetTypeAndTargetIdInAndStatus(
                CouponTargetType.PRODUCT,
                productIds,
                EntityStatus.ACTIVE
        );

        List<CouponTargetEntity> categoryTargets = couponTargetRepository.findByTargetTypeAndTargetIdInAndStatus(
                CouponTargetType.PRODUCT_CATEGORY,
                productCategoryRepository.findByProductIdInAndStatus(productIds, EntityStatus.ACTIVE).stream()
                        .map(it -> it.getCategoryId())
                        .collect(Collectors.toList()),
                EntityStatus.ACTIVE
        );

        return couponRepository.findByIdInAndStatus(
                        Stream.concat(productTargets.stream(), categoryTargets.stream())
                                .map(CouponTargetEntity::getCouponId)
                                .collect(Collectors.toSet()),
                        EntityStatus.ACTIVE).stream()
                .map(it -> new Coupon(it.getId(), it.getName(), it.getType(), it.getDiscount(), it.getExpiredAt()))
                .collect(Collectors.toList());
    }
}
