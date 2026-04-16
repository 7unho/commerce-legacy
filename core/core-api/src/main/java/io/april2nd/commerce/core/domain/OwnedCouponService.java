package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OwnedCouponState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CouponEntity;
import io.april2nd.commerce.storage.db.core.CouponRepository;
import io.april2nd.commerce.storage.db.core.OwnedCouponEntity;
import io.april2nd.commerce.storage.db.core.OwnedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnedCouponService {
    private CouponRepository couponRepository;
    private OwnedCouponRepository ownedCouponRepository;

    public void download(User user, Long couponId) {
        CouponEntity coupon = couponRepository.findByIdAndStatusAndExpiredAtAfter(couponId, EntityStatus.ACTIVE, LocalDateTime.now())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        OwnedCouponEntity existing = ownedCouponRepository.findByUserIdAndCouponId(user.id(), couponId);
        if (existing != null) {
            throw new CoreException(ErrorType.COUPON_ALREADY_DOWNLOADED);
        }
        ownedCouponRepository.save(
                new OwnedCouponEntity(
                        user.id(),
                        coupon.getId(),
                        OwnedCouponState.DOWNLOADED
                )
        );
    }

    public List<OwnedCoupon> getOwnedCoupons(User user) {
        List<OwnedCouponEntity> ownedCoupons = ownedCouponRepository.findByUserIdAndStatus(user.id(), EntityStatus.ACTIVE);
        if (ownedCoupons.isEmpty()) return Collections.emptyList();

        Map<Long, CouponEntity> couponMap = couponRepository.findAllById(ownedCoupons.stream()
                        .map(it -> it.getCouponId())
                        .collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(
                        CouponEntity::getId,
                        Function.identity()
                ));

        return ownedCoupons.stream()
                .map(it ->
                        new OwnedCoupon(
                                it.getId(),
                                it.getUserId(),
                                it.getState(),
                                new Coupon(
                                        couponMap.get(it.getCouponId()).getId(),
                                        couponMap.get(it.getCouponId()).getName(),
                                        couponMap.get(it.getCouponId()).getType(),
                                        couponMap.get(it.getCouponId()).getDiscount(),
                                        couponMap.get(it.getCouponId()).getExpiredAt()
                                )
                        )
                )
                .collect(Collectors.toList());
    }

    public List<OwnedCoupon> getOwnedCouponsForCheckout(User user, Collection<Long> productIds) {
        if (productIds.isEmpty()) return Collections.emptyList();

        Map<Long, CouponEntity> applicableCouponMap = couponRepository.findApplicableCouponIds(productIds).stream()
                .collect(Collectors.toMap(
                        CouponEntity::getId,
                        Function.identity()
                ));
        if (applicableCouponMap.isEmpty()) return Collections.emptyList();

        List<OwnedCouponEntity> ownedCoupons = ownedCouponRepository.findOwnedCouponIds(user.id(), applicableCouponMap.keySet(), LocalDateTime.now());
        if (ownedCoupons.isEmpty()) return Collections.emptyList();

        return ownedCoupons.stream()
                .map(it -> new OwnedCoupon(
                        it.getId(),
                        it.getUserId(),
                        it.getState(),
                        new Coupon(
                                applicableCouponMap.get(it.getCouponId()).getId(),
                                applicableCouponMap.get(it.getCouponId()).getName(),
                                applicableCouponMap.get(it.getCouponId()).getType(),
                                applicableCouponMap.get(it.getCouponId()).getDiscount(),
                                applicableCouponMap.get(it.getCouponId()).getExpiredAt()
                        )
                ))
                .collect(Collectors.toList());
    }
}
