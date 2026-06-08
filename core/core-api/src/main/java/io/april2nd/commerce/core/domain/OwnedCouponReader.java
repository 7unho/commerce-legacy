package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.CouponEntity;
import io.april2nd.commerce.storage.db.core.CouponRepository;
import io.april2nd.commerce.storage.db.core.OwnedCouponEntity;
import io.april2nd.commerce.storage.db.core.OwnedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OwnedCouponReader {
    private final CouponRepository couponRepository;
    private final OwnedCouponRepository ownedCouponRepository;

    public List<OwnedCoupon> getOwnedCoupons(Long userId) {
        List<OwnedCouponEntity> owned = ownedCouponRepository.findByUserIdAndStatus(userId, EntityStatus.ACTIVE);
        if (owned.isEmpty()) return Collections.emptyList();

        Set<Long> ownedCouponIds = owned.stream().map(OwnedCouponEntity::getId).collect(Collectors.toSet());
        Map<Long, CouponEntity> couponMap = couponRepository.findAllById(ownedCouponIds)
                .stream()
                .collect(Collectors.toMap(
                        CouponEntity::getId,
                        Function.identity()
                ));

        return owned.stream()
                .map(it -> {
                    CouponEntity couponEntity = couponMap.get(it.getCouponId());

                    return new OwnedCoupon(
                            it.getId(),
                            it.getUserId(),
                            it.getState(),
                            it.getMaxUseCount(),
                            it.usedCount(),
                            new Coupon(
                                    couponEntity.getId(),
                                    couponEntity.getName(),
                                    couponEntity.getType(),
                                    couponEntity.getDiscount(),
                                    couponEntity.getMinOrderAmount(),
                                    couponEntity.getExpiredAt()
                            )
                    );
                })
                .collect(Collectors.toList());
    }

    public List<OwnedCoupon> findOwnedForCheckout(Long userId, Collection<Long> couponIds, LocalDateTime now) {
        if (couponIds.isEmpty()) return Collections.emptyList();

        List<OwnedCouponEntity> owned = ownedCouponRepository.findOwnedCouponIds(userId, couponIds, now);
        if (owned.isEmpty()) return Collections.emptyList();

        Set<Long> ownedCouponIds = owned.stream().map(OwnedCouponEntity::getId).collect(Collectors.toSet());
        Map<Long, CouponEntity> couponMap = couponRepository.findAllById(ownedCouponIds)
                .stream()
                .collect(Collectors.toMap(
                        CouponEntity::getId,
                        Function.identity()
                ));

        return owned.stream()
                .map(it -> {
                    CouponEntity couponEntity = couponMap.get(it.getCouponId());

                    return new OwnedCoupon(
                            it.getId(),
                            it.getUserId(),
                            it.getState(),
                            it.getMaxUseCount(),
                            it.usedCount(),
                            new Coupon(
                                    couponEntity.getId(),
                                    couponEntity.getName(),
                                    couponEntity.getType(),
                                    couponEntity.getDiscount(),
                                    couponEntity.getMinOrderAmount(),
                                    couponEntity.getExpiredAt()
                            )
                    );
                })
                .collect(Collectors.toList());
    }
}
