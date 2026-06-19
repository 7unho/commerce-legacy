package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.PointType;
import io.april2nd.commerce.core.enums.TransactionType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CancelProcessor {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OwnedCouponUsageManager ownedCouponUsageManager;
    private final CancelRepository cancelRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final PointHandler pointHandler;

    @Transactional
    public Long cancel(CancelAction action) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(action.orderKey(), OrderState.PAID, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        order.canceled();

        if (payment.hasAppliedCoupon()) {
            ownedCouponUsageManager.revert(payment.getOwnedCouponId());
        }

        User user = new User(payment.getUserId());
        pointHandler.earn(user, PointType.PAYMENT, payment.getId(), payment.getUsedPoint());
        pointHandler.deduct(user, PointType.PAYMENT, payment.getId(), PointAmount.PAYMENT);

        CancelEntity cancel = cancelRepository.save(
                new CancelEntity(
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        payment.getOriginAmount(),
                        payment.getOwnedCouponId(),
                        payment.getCouponDiscount(),
                        payment.getUsedPoint(),
                        payment.getPaidAmount(),
                        payment.getPaidAmount(),
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
                        "취소 성공",
                        cancel.getCanceledAt()
                )
        );

        return cancel.getId();
    }
}
