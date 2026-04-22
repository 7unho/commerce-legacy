package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CouponTargetType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "coupon_target")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CouponTargetEntity extends BaseEntity {
    private Long couponId;

    @Enumerated(EnumType.STRING)
    private CouponTargetType targetType;

    private Long targetId;
}