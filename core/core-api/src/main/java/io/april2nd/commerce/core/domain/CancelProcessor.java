package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.CancelType;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.PointType;
import io.april2nd.commerce.core.enums.TransactionType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CancelProcessor {
    private final OrderRepository orderRepository;
    private final OrderManager orderManager;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OwnedCouponUsageManager ownedCouponUsageManager;
    private final CancelRepository cancelRepository;
    private final CancelBalanceRepository cancelBalanceRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final PointHandler pointHandler;

    @Transactional
    public Long cancel(CancelAction action) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(action.orderKey(), OrderState.PAID, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        CancelBalanceEntity cancelBalance = cancelBalanceRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        cancelBalance.cancel(payment.getPaidAmount(), payment.getUsedPoint(), payment.getCouponDiscount());

        order.canceled();
        orderManager.cancelAllOrderItems(order.getId());

        if (payment.hasAppliedCoupon()) {
            ownedCouponUsageManager.revert(payment.getOwnedCouponId());
        }

        User user = new User(payment.getUserId());
        pointHandler.earn(user, PointType.PAYMENT, payment.getId(), payment.getUsedPoint());
        pointHandler.deduct(user, PointType.PAYMENT, payment.getId(), PointAmount.PAYMENT);

        CancelEntity cancel = cancelRepository.save(
                new CancelEntity(
                        CancelType.ALL,
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        payment.getOriginAmount(),
                        payment.getOwnedCouponId(),
                        payment.getCouponDiscount(),
                        payment.getUsedPoint(),
                        payment.getPaidAmount(),
                        payment.getPaidAmount(),
                        payment.getUsedPoint(),
                        payment.getCouponDiscount(),
                        "PG_API_응답_취소_고유_값_저장",
                        LocalDateTime.now()
                )
        );

        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        TransactionType.CANCEL,
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        Objects.requireNonNull(payment.getExternalPaymentKey()),
                        payment.getPaidAmount(),
                        payment.getUsedPoint(),
                        payment.getCouponDiscount(),
                        payment.getUsedPoint(),
                        payment.getCouponDiscount(),
                        "취소 성공",
                        cancel.getCanceledAt()
                )
        );

        return cancel.getId();
    }

    @Transactional
    public Long partialCancel(PartialCancelAction action, CancelCalculateResult calculateResult) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(action.orderKey(), OrderState.PAID, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        CancelBalanceEntity cancelBalance = cancelBalanceRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        OrderItemEntity orderItem = orderItemRepository.findById(action.orderItemId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!order.getId().equals(orderItem.getOrderId())) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        orderManager.cancelOrderItem(order.getId(), action.orderItemId(), action.quantity());

        cancelBalance.cancel(calculateResult.paidAmount(), calculateResult.pointAmount(), calculateResult.couponAmount());

        if (calculateResult.shouldRestoreCoupon()) {
            ownedCouponUsageManager.revert(payment.getOwnedCouponId());
        }

        CancelEntity cancel = cancelRepository.save(
                new CancelEntity(
                        CancelType.PARTIAL,
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        orderItem.getUnitPrice().multiply(BigDecimal.valueOf(action.quantity())),
                        payment.getOwnedCouponId(),
                        calculateResult.couponAmount(),
                        calculateResult.pointAmount(),
                        calculateResult.paidAmount(),
                        calculateResult.paidAmount(),
                        calculateResult.pointAmount(),
                        calculateResult.couponAmount(),
                        "PG_API_응답_부분_취소_고유_값_저장",
                        LocalDateTime.now()
                )
        );

        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        TransactionType.PARTIAL_CANCELED,
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        Objects.requireNonNull(payment.getExternalPaymentKey()),
                        calculateResult.paidAmount(),
                        calculateResult.pointAmount(),
                        calculateResult.couponAmount(),
                        calculateResult.pointAmount(),
                        calculateResult.couponAmount(),
                        "부분 취소 성공",
                        cancel.getCanceledAt()
                )
        );

        return cancel.getId();
    }
}
