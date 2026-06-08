package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.OwnedCouponState;
import io.april2nd.commerce.storage.db.core.error.IllegalCouponUsageException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "owned_coupon",
        indexes = {
                @Index(name = "udx_owned_coupon", columnList = "userId, couponId", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnedCouponEntity extends BaseEntity {

    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, updatable = false)
    private Long couponId;

    @Setter(AccessLevel.PROTECTED)
    @Column(nullable = false)
    private OwnedCouponState state;

    @Column(nullable = false, updatable = false)
    private Long maxUseCount;

    @Column(nullable = false)
    @Getter(AccessLevel.NONE)
    private Long usedCount;

    @Version
    private Long version = 0L;

    @Builder
    public OwnedCouponEntity(Long userId, Long couponId, OwnedCouponState state, Long maxUseCount, Long usedCount) {
        this.userId = userId;
        this.couponId = couponId;
        this.state = state;
        this.maxUseCount = maxUseCount;
        this.usedCount = usedCount;
    }

    public Long usedCount() {
        return usedCount;
    }

    public void use() {
        if (isFullyUsed()) {
            throw new IllegalCouponUsageException("Coupon cannot be used anymore");
        }
        this.usedCount += 1L;
        if (isFullyUsed()) {
            this.state = OwnedCouponState.USED;
        }
    }

    public void revert() {
        if (isUnused()) {
            throw new IllegalCouponUsageException("Coupon cannot be reverted because it has not been used");
        }
        this.usedCount -= 1L;
        if (isUnused()) {
            this.state = OwnedCouponState.DOWNLOADED;
        }
    }

    private boolean isFullyUsed() {
        return (this.maxUseCount - this.usedCount) == 0L;
    }

    private boolean isUnused() {
        return this.usedCount == 0L;
    }
}