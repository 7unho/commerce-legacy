package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "cancel_balance",
        indexes = {
                @Index(name = "udx_cancel_balance_order_id", columnList = "orderId", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CancelBalanceEntity extends BaseEntity {
    private Long orderId;
    private Long paymentId;
    private BigDecimal cancellablePaidAmount;
    private BigDecimal cancellablePointAmount;
    private BigDecimal cancellableCouponAmount;
    private BigDecimal cancelledPaidAmount;
    private BigDecimal cancelledPointAmount;
    private BigDecimal cancelledCouponAmount;

    @Version
    private Long version = 0L;

    public CancelBalanceEntity(
            Long orderId,
            Long paymentId,
            BigDecimal cancellablePaidAmount,
            BigDecimal cancellablePointAmount,
            BigDecimal cancellableCouponAmount
    ) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.cancellablePaidAmount = cancellablePaidAmount;
        this.cancellablePointAmount = cancellablePointAmount;
        this.cancellableCouponAmount = cancellableCouponAmount;
        this.cancelledPaidAmount = BigDecimal.ZERO;
        this.cancelledPointAmount = BigDecimal.ZERO;
        this.cancelledCouponAmount = BigDecimal.ZERO;
    }

    public void cancel(BigDecimal paidAmount, BigDecimal pointAmount, BigDecimal couponAmount) {
        this.cancellablePaidAmount = cancellablePaidAmount.subtract(paidAmount);
        this.cancellablePointAmount = cancellablePointAmount.subtract(pointAmount);
        this.cancellableCouponAmount = cancellableCouponAmount.subtract(couponAmount);
        this.cancelledPaidAmount = cancelledPaidAmount.add(paidAmount);
        this.cancelledPointAmount = cancelledPointAmount.add(pointAmount);
        this.cancelledCouponAmount = cancelledCouponAmount.add(couponAmount);
    }

    public BigDecimal totalCanceledAmount() {
        return cancelledPaidAmount
                .add(cancelledPointAmount)
                .add(cancelledCouponAmount);
    }
}
