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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                        it -> it.getId(),
                        it -> it
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
}
