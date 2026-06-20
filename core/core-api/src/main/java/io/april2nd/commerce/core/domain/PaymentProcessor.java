package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.*;
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
public class PaymentProcessor {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PointHandler pointHandler;
    private final OwnedCouponUsageManager ownedCouponUsageManager;
    private final CancelBalanceRepository cancelBalanceRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    @Transactional
    public Long success(String orderKey, String externalPaymentKey) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        payment.success(
                externalPaymentKey,
                PaymentMethod.CARD,
                "PG 승인 API 호출의 응답 값 중 `승인번호` 넣기"
        );

        order.paid();
        orderItemRepository.findByOrderId(order.getId())
                .stream()
                .forEach(OrderItemEntity::paid);

        if (payment.hasAppliedCoupon()) {
            ownedCouponUsageManager.use(payment.getOwnedCouponId());
        }

        pointHandler.deduct(new User(payment.getUserId()), PointType.PAYMENT, payment.getId(), payment.getUsedPoint());
        pointHandler.earn(new User(payment.getUserId()), PointType.PAYMENT, payment.getId(), PointAmount.PAYMENT);

        cancelBalanceRepository.save(
                new CancelBalanceEntity(
                        payment.getOrderId(),
                        payment.getId(),
                        payment.getPaidAmount(),
                        payment.getUsedPoint(),
                        payment.getCouponDiscount()
                )
        );

        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        TransactionType.PAYMENT,
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        externalPaymentKey,
                        payment.getPaidAmount(),
                        payment.getUsedPoint(),
                        payment.getCouponDiscount(),
                        "결제 성공",
                        Objects.requireNonNull(payment.getPaidAt())
                )
        );

        return payment.getId();
    }

    @Transactional
    public void fail(String orderKey, String code, String message) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        TransactionType.PAYMENT_FAIL,
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        "",
                        BigDecimal.valueOf(-1),
                        BigDecimal.valueOf(-1),
                        BigDecimal.valueOf(-1),
                        "[%s] %s".formatted(code, message),
                        LocalDateTime.now()
                )
        );
    }
}
