package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CouponType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
public class PaymentDiscount {
    @Getter(AccessLevel.NONE)
    private final List<OwnedCoupon> ownedCoupons;

    @Getter(AccessLevel.NONE)
    private final PointBalance pointBalance;

    private final long useOwnedCouponId;

    @Getter(AccessLevel.NONE)
    private final BigDecimal usePointAmount;

    private final CouponType couponType;
    private final BigDecimal couponDiscount;
    private final BigDecimal couponMinOrderAmount;
    private final BigDecimal usePoint;

    public static final PaymentDiscount EMPTY = new PaymentDiscount(
            Collections.emptyList(),
            new PointBalance(-1L, BigDecimal.ZERO),
            -1,
            BigDecimal.ZERO
    );

    public PaymentDiscount(List<OwnedCoupon> ownedCoupons, PointBalance pointBalance, long useOwnedCouponId, BigDecimal usePointAmount) {
        this.ownedCoupons = ownedCoupons;
        this.pointBalance = pointBalance;
        this.useOwnedCouponId = useOwnedCouponId;
        this.usePointAmount = usePointAmount;

        if (useOwnedCouponId > 0) {
            OwnedCoupon ownedCoupon = ownedCoupons.stream()
                    .filter(it -> it.id() == useOwnedCouponId)
                    .findFirst()
                    .orElseThrow(() -> new CoreException(ErrorType.OWNED_COUPON_INVALID));

            this.couponType = ownedCoupon.coupon().type();
            this.couponDiscount = ownedCoupon.coupon().discount();
            this.couponMinOrderAmount = ownedCoupon.coupon().minOrderAmount();
        } else {
            this.couponType = CouponType.NONE;
            this.couponDiscount = BigDecimal.ZERO;
            this.couponMinOrderAmount = BigDecimal.ZERO;
        }

        if (usePointAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (usePointAmount.compareTo(pointBalance.balance()) > 0) throw new CoreException(ErrorType.POINT_EXCEEDS_BALANCE);
            this.usePoint = usePointAmount;
        } else {
            this.usePoint = BigDecimal.ZERO;
        }
    }

    public BigDecimal paidAmount(BigDecimal orderPrice) {
        if (orderPrice.compareTo(couponMinOrderAmount) < 0) throw new CoreException(ErrorType.OWNED_COUPON_MIN_AMOUNT_NOT_REACHED);

        BigDecimal couponDiscountAmount = switch (couponType) {
            case NONE -> BigDecimal.ZERO;
            case FIXED_AMOUNT -> couponDiscount;
            case PERCENT_RATE -> orderPrice.multiply(couponDiscount);
        };

        BigDecimal amount = orderPrice.subtract(couponDiscountAmount.add(usePointAmount));

        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new CoreException(ErrorType.PAYMENT_INVALID_AMOUNT);

        return amount;
    }
}
