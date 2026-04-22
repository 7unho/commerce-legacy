package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Getter
public class PaymentDiscount {
    private final List<OwnedCoupon> ownedCoupons;
    private final PointBalance pointBalance;
    private final Long useOwnedCouponId;
    private final BigDecimal usePointAmount;

    private final BigDecimal couponDiscount;
    private final BigDecimal usePoint;

    public PaymentDiscount(List<OwnedCoupon> ownedCoupons, PointBalance pointBalance, Long useOwnedCouponId, BigDecimal usePointAmount) {
        this.ownedCoupons = ownedCoupons;
        this.pointBalance = pointBalance;
        this.useOwnedCouponId = useOwnedCouponId;
        this.usePointAmount = usePointAmount;

        // 쿠폰 할인 계산
        if (useOwnedCouponId > 0) {
            OwnedCoupon found = ownedCoupons.stream()
                    .filter(c -> Objects.equals(c.id(), useOwnedCouponId))
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                throw new CoreException(ErrorType.OWNED_COUPON_INVALID);
            }
            this.couponDiscount = found.coupon().discount();
        } else {
            this.couponDiscount = BigDecimal.ZERO;
        }

        // 포인트 사용액 계산
        if (usePointAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (usePointAmount.compareTo(pointBalance.balance()) > 0) {
                throw new CoreException(ErrorType.POINT_EXCEEDS_BALANCE);
            }
            this.usePoint = usePointAmount;
        } else {
            this.usePoint = BigDecimal.ZERO;
        }
    }

    public BigDecimal paidAmount(BigDecimal orderPrice) {
        BigDecimal amount = orderPrice.subtract(couponDiscount.add(usePointAmount));
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.PAYMENT_INVALID_AMOUNT);
        }
        return amount;
    }
}
