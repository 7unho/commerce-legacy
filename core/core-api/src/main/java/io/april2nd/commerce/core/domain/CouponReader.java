package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CouponReader {
    private final CouponRepository couponRepository;

    public List<Coupon> findActiveByIds(Collection<Long> couponIds) {
        if (couponIds.isEmpty()) return Collections.emptyList();

        return couponRepository.findByIdInAndStatus(couponIds, EntityStatus.ACTIVE)
                .stream()
                .map(it ->
                        new Coupon(
                                it.getId(),
                                it.getName(),
                                it.getType(),
                                it.getDiscount(),
                                it.getMinOrderAmount(),
                                it.getExpiredAt()
                        ))
                .collect(Collectors.toList());
    }
}
