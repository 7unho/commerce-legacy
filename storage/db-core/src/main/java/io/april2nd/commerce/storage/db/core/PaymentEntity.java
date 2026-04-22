package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.PaymentMethod;
import io.april2nd.commerce.core.enums.PaymentState;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment",
        indexes = {
                @Index(name = "udx_order_id", columnList = "orderId", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity extends BaseEntity {

    private Long userId;
    private Long orderId;
    private BigDecimal originAmount;
    private Long ownedCouponId;
    private BigDecimal couponDiscount;
    private BigDecimal usedPoint;
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    private PaymentState state;

    private String externalPaymentKey;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String approveCode;

    private LocalDateTime paidAt;

    @Builder
    public PaymentEntity(
            Long userId,
            Long orderId,
            BigDecimal originAmount,
            Long ownedCouponId,
            BigDecimal couponDiscount,
            BigDecimal usedPoint,
            BigDecimal paidAmount,
            PaymentState state,
            String externalPaymentKey,
            PaymentMethod method,
            String approveCode,
            LocalDateTime paidAt
    ) {
        this.userId = userId;
        this.orderId = orderId;
        this.originAmount = originAmount;
        this.ownedCouponId = ownedCouponId;
        this.couponDiscount = couponDiscount;
        this.usedPoint = usedPoint;
        this.paidAmount = paidAmount;
        this.state = state;
        this.externalPaymentKey = externalPaymentKey;
        this.method = method;
        this.approveCode = approveCode;
        this.paidAt = paidAt;
    }

    public void success(String externalPaymentKey, PaymentMethod method, String approveCode) {
        this.state = PaymentState.SUCCESS;
        this.externalPaymentKey = externalPaymentKey;
        this.method = method;
        this.approveCode = approveCode;
        this.paidAt = LocalDateTime.now();
    }

    public boolean hasAppliedCoupon() {
        return ownedCouponId != null && ownedCouponId > 0;
    }
}