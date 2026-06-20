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
    private final OwnedCouponReader ownedCouponReader;

    public CancelCalculated calculatePartial(PartialCancelAction action) {
        OrderEntity order = orderRepository.findByOrderKeyAndStatus(action.orderKey(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (order.getState() != OrderState.PAID && order.getState() != OrderState.PARTIAL_CANCELED) {
            throw new CoreException(ErrorType.PAYMENT_INVALID_STATE);
        }

        OrderItemEntity targetItem = orderItemRepository.findById(action.orderItemId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!order.getId().equals(targetItem.getOrderId())) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        CancelBalanceEntity cancelBalance = cancelBalanceRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        CancelAmount cancelAmount = new CancelAmount(
                targetItem.getUnitPrice().multiply(BigDecimal.valueOf(action.quantity())),
                order.getTotalPrice(),
                cancelBalance.totalCanceledAmount(),
                cancelBalance.getCancellablePaidAmount(),
                cancelBalance.getCancellablePointAmount(),
                cancelBalance.getCancellableCouponAmount(),
                getCouponMinimumOrderAmount(payment)
        );

        return new CancelCalculated(
                cancelAmount.paidAmount(),
                cancelAmount.couponAmount(),
                cancelAmount.pointAmount(),
                cancelAmount.isRestoreCoupon()
        );
    }


    private BigDecimal getCouponMinimumOrderAmount(PaymentEntity payment) {
        if (!payment.hasAppliedCoupon()) return BigDecimal.ZERO;

        return ownedCouponReader.getOwnedCoupon(payment.getOwnedCouponId())
                .coupon()
                .minOrderAmount();
    }
}
