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
    private BigDecimal cancelablePaidAmount;
    private BigDecimal cancelablePointAmount;
    private BigDecimal cancelableCouponAmount;
    private BigDecimal canceledPaidAmount;
    private BigDecimal canceledPointAmount;
    private BigDecimal canceledCouponAmount;

    @Version
    private Long version = 0L;

    public CancelBalanceEntity(
            Long orderId,
            Long paymentId,
            BigDecimal cancelablePaidAmount,
            BigDecimal cancelablePointAmount,
            BigDecimal cancelableCouponAmount
    ) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.cancelablePaidAmount = cancelablePaidAmount;
        this.cancelablePointAmount = cancelablePointAmount;
        this.cancelableCouponAmount = cancelableCouponAmount;
        this.canceledPaidAmount = BigDecimal.ZERO;
        this.canceledPointAmount = BigDecimal.ZERO;
        this.canceledCouponAmount = BigDecimal.ZERO;
    }

    public void apply(BigDecimal paidAmount, BigDecimal pointAmount, BigDecimal couponAmount) {
        this.cancelablePaidAmount = cancelablePaidAmount.subtract(paidAmount);
        this.cancelablePointAmount = cancelablePointAmount.subtract(pointAmount);
        this.cancelableCouponAmount = cancelableCouponAmount.subtract(couponAmount);
        this.canceledPaidAmount = canceledPaidAmount.add(paidAmount);
        this.canceledPointAmount = canceledPointAmount.add(pointAmount);
        this.canceledCouponAmount = canceledCouponAmount.add(couponAmount);
    }
}
