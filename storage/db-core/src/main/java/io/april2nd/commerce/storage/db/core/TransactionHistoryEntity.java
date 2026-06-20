package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionHistoryEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Long userId;
    private Long orderId;
    private Long paymentId;
    private String externalPaymentKey;
    private BigDecimal paidAmount;
    private BigDecimal pointAmount;
    private BigDecimal couponAmount;
    private BigDecimal canceledPointAmount;
    private BigDecimal canceledCouponAmount;
    private String message;
    private LocalDateTime occurredAt;

    public TransactionHistoryEntity(
            TransactionType type,
            Long userId,
            Long orderId,
            Long paymentId,
            String externalPaymentKey,
            BigDecimal paidAmount,
            BigDecimal pointAmount,
            BigDecimal couponAmount,
            String message,
            LocalDateTime occurredAt
    ) {
        this(
                type,
                userId,
                orderId,
                paymentId,
                externalPaymentKey,
                paidAmount,
                pointAmount,
                couponAmount,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                message,
                occurredAt
        );
    }

    public TransactionHistoryEntity(
            TransactionType type,
            Long userId,
            Long orderId,
            Long paymentId,
            String externalPaymentKey,
            BigDecimal paidAmount,
            BigDecimal pointAmount,
            BigDecimal couponAmount,
            BigDecimal canceledPointAmount,
            BigDecimal canceledCouponAmount,
            String message,
            LocalDateTime occurredAt
    ) {
        this.type = type;
        this.userId = userId;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.externalPaymentKey = externalPaymentKey;
        this.paidAmount = paidAmount;
        this.pointAmount = pointAmount;
        this.couponAmount = couponAmount;
        this.canceledPointAmount = canceledPointAmount;
        this.canceledCouponAmount = canceledCouponAmount;
        this.message = message;
        this.occurredAt = occurredAt;
    }
}
