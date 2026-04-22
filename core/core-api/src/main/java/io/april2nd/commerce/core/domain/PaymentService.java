package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.*;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final PointHandler pointHandler;
    private final OwnedCouponRepository ownedCouponRepository;

    @Transactional
    public Long createPayment(Order order, PaymentDiscount paymentDiscount) {
        PaymentEntity found = paymentRepository.findByOrderId(order.id())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (found.getState() == PaymentState.SUCCESS) {
            throw new CoreException(ErrorType.ORDER_ALREADY_PAID);
        }

        PaymentEntity payment = PaymentEntity.builder()
                .userId(order.userId())
                .orderId(order.id())
                .originAmount(order.totalPrice())
                .ownedCouponId(paymentDiscount.getUseOwnedCouponId())
                .couponDiscount(paymentDiscount.getCouponDiscount())
                .usedPoint(paymentDiscount.getUsePoint())
                .paidAmount(paymentDiscount.paidAmount(order.totalPrice()))
                .state(PaymentState.READY)
                .build();

        return paymentRepository.save(payment).getId();
    }

    @Transactional
    public Long success(String orderKey, String externalPaymentKey, BigDecimal amount) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!Objects.equals(order.getUserId(), payment.getUserId())) throw new CoreException(ErrorType.NOT_FOUND_DATA);
        if (payment.getState() != PaymentState.READY) throw new CoreException(ErrorType.NOT_FOUND_DATA);
        if (payment.getPaidAmount().compareTo(amount) != 0)
            throw new CoreException(ErrorType.PAYMENT_AMOUNT_MISMATCH);

        /**
         * NOTE: PG 승인 API 호출 => 성공 시 다음 로직으로 진행 | 실패 시 예외 발생
         */

        payment.success(
                externalPaymentKey,
                // NOTE: PG 승인 API 호출의 응답 값 중 `결제 수단` 넣기
                PaymentMethod.CARD,
                "PG 승인 API 호출의 응답 값 중 `승인번호` 넣기"
        );
        order.paid();

        if (payment.hasAppliedCoupon()) {
            ownedCouponRepository.findById(payment.getOwnedCouponId()).ifPresent(OwnedCouponEntity::use);
        }

        pointHandler.deduct(new User(payment.getUserId()), PointType.PAYMENT, payment.getId(), payment.getUsedPoint());
        pointHandler.earn(new User(payment.getUserId()), PointType.PAYMENT, payment.getId(), PointAmount.PAYMENT);

        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        TransactionType.PAYMENT,
                        order.getUserId(),
                        order.getId(),
                        payment.getId(),
                        externalPaymentKey,
                        payment.getPaidAmount(),
                        "결제 성공",
                        Objects.requireNonNull(payment.getPaidAt())
                )
        );

        return payment.getId();
    }

    public void fail(String orderKey, String code, String message) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        TransactionType.PAYMENT_FAIL,
                        order.getUserId(),
                        order.getId(),
                        payment.getId(),
                        "",
                        BigDecimal.valueOf(-1),
                        "[%s] %s".formatted(code, message),
                        LocalDateTime.now()
                )
        );
    }
}
