package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CancelType;
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
@Table(name = "cancel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CancelEntity extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private CancelType type;
    private Long userId;
    private Long orderId;
    private Long orderItemId; // NOTE: ALL -> -1
    private Long paymentId;
    private BigDecimal originAmount;
    private Long ownedCouponId;
    private BigDecimal couponDiscount;
    private BigDecimal usedPoint;
    private BigDecimal paidAmount;
    private Long canceledQuantity;
    private BigDecimal canceledPaidAmount;
    private BigDecimal canceledPointAmount;
    private BigDecimal canceledCouponAmount;
    private String externalCancelKey;
    private LocalDateTime canceledAt;
}
