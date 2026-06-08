package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CouponType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CouponEntity extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private BigDecimal discount;

    private Long maxUseCount;

    private BigDecimal minOrderAmount;

    private LocalDateTime expiredAt;
}