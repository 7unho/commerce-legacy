package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CancelType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CancelEntity extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private CancelType type;
    private Long userId;
    private Long orderId;
    private Long paymentId;
    private BigDecimal originAmount;
    private Long ownedCouponId;
    private BigDecimal couponDiscount;
    private BigDecimal usedPoint;
    private BigDecimal paidAmount;
    private BigDecimal canceledAmount;
    private BigDecimal canceledPointAmount;
    private BigDecimal canceledCouponAmount;
    private String externalCancelKey;
    private LocalDateTime canceledAt;

    public CancelEntity(
            CancelType type,
            Long userId,
            Long orderId,
            Long paymentId,
            BigDecimal originAmount,
            Long ownedCouponId,
            BigDecimal couponDiscount,
            BigDecimal usedPoint,
            BigDecimal paidAmount,
            BigDecimal canceledAmount,
            BigDecimal canceledPointAmount,
            BigDecimal canceledCouponAmount,
            String externalCancelKey,
            LocalDateTime canceledAt
    ) {
        this.type = type;
        this.userId = userId;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.originAmount = originAmount;
        this.ownedCouponId = ownedCouponId;
        this.couponDiscount = couponDiscount;
        this.usedPoint = usedPoint;
        this.paidAmount = paidAmount;
        this.canceledAmount = canceledAmount;
        this.canceledPointAmount = canceledPointAmount;
        this.canceledCouponAmount = canceledCouponAmount;
        this.externalCancelKey = externalCancelKey;
        this.canceledAt = canceledAt;
    }
}
