package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CancelEntity extends BaseEntity {
    private Long userId;
    private Long orderId;
    private Long paymentId;
    private BigDecimal originAmount;
    private Long ownedCouponId;
    private BigDecimal couponDiscount;
    private BigDecimal usedPoint;
    private BigDecimal paidAmount;
    private BigDecimal canceledAmount;
    private String externalCancelKey;
    private LocalDateTime canceledAt;
}