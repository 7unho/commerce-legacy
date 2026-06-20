package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CancelCalculator {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CancelBalanceRepository cancelBalanceRepository;
    private final OwnedCouponRepository ownedCouponRepository;
    private final CouponRepository couponRepository;

    public CancelCalculateResult calculatePartial(String orderKey, Long orderItemId, Long cancelQuantity) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.PAID, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        if (!order.getId().equals(orderItem.getOrderId())) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        CancelBalanceEntity balance = cancelBalanceRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        BigDecimal remainingOrderAmountAfterCancel = payment.getOriginAmount()
                .subtract(balance.getCanceledPaidAmount())
                .subtract(balance.getCanceledPointAmount())
                .subtract(balance.getCanceledCouponAmount())
                .subtract(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(cancelQuantity)));

        return calculate(
                orderItem.getUnitPrice().multiply(BigDecimal.valueOf(cancelQuantity)),
                remainingOrderAmountAfterCancel,
                getCouponMinimumOrderAmount(payment),
                balance.getCancelablePaidAmount(),
                balance.getCancelableCouponAmount(),
                balance.getCancelablePointAmount()
        );
    }

    CancelCalculateResult calculate(
            BigDecimal cancelAmount,
            BigDecimal remainingOrderAmountAfterCancel,
            BigDecimal couponMinimumOrderAmount,
            BigDecimal remainingPaidAmount,
            BigDecimal remainingCouponAmount,
            BigDecimal remainingPointAmount
    ) {
        BigDecimal remainingCancelAmount = cancelAmount;

        BigDecimal cancelCouponAmount = BigDecimal.ZERO;
        if (remainingCouponAmount.compareTo(BigDecimal.ZERO) > 0
                && remainingOrderAmountAfterCancel.compareTo(couponMinimumOrderAmount) < 0) {
            cancelCouponAmount = min(remainingCancelAmount, remainingCouponAmount);
            remainingCancelAmount = remainingCancelAmount.subtract(cancelCouponAmount);
        }

        BigDecimal cancelPaidAmount = min(remainingCancelAmount, remainingPaidAmount);
        remainingCancelAmount = remainingCancelAmount.subtract(cancelPaidAmount);

        BigDecimal cancelPointAmount = min(remainingCancelAmount, remainingPointAmount);
        remainingCancelAmount = remainingCancelAmount.subtract(cancelPointAmount);

        if (remainingCancelAmount.compareTo(BigDecimal.ZERO) > 0) {
            throw new CoreException(ErrorType.PAYMENT_INVALID_AMOUNT);
        }

        return new CancelCalculateResult(
                cancelPaidAmount,
                cancelCouponAmount,
                cancelPointAmount,
                cancelCouponAmount.compareTo(BigDecimal.ZERO) > 0
        );
    }

    private BigDecimal getCouponMinimumOrderAmount(PaymentEntity payment) {
        if (!payment.hasAppliedCoupon()) return BigDecimal.ZERO;

        OwnedCouponEntity ownedCoupon = ownedCouponRepository.findById(payment.getOwnedCouponId())
                .orElseThrow(() -> new CoreException(ErrorType.OWNED_COUPON_INVALID));
        CouponEntity coupon = couponRepository.findById(ownedCoupon.getCouponId())
                .orElseThrow(() -> new CoreException(ErrorType.COUPON_NOT_FOUND_OR_EXPIRED));

        return coupon.getMinOrderAmount();
    }

    private BigDecimal min(BigDecimal amount, BigDecimal limit) {
        return amount.compareTo(limit) < 0 ? amount : limit;
    }
}
