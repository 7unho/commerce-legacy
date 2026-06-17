package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.*;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PaymentManager {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PointHandler pointHandler;
    private final OwnedCouponUsageManager ownedCouponUsageManager;

    @Transactional
    public Payment success(String orderKey, String externalPaymentKey, BigDecimal amount) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        PaymentEntity paymentEntity = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        paymentEntity.success(
                externalPaymentKey,
                // NOTE: PG 승인 API 호출의 응답 값 중 `결제 수단` 넣기
                PaymentMethod.CARD,
                "PG 승인 API 호출의 응답 값 중 `승인번호` 넣기"
        );
        order.paid();

        if (paymentEntity.hasAppliedCoupon()) {
            ownedCouponUsageManager.use(paymentEntity.getOwnedCouponId());
        }

        pointHandler.deduct(new User(paymentEntity.getUserId()), PointType.PAYMENT, paymentEntity.getId(), paymentEntity.getUsedPoint());
        pointHandler.earn(new User(paymentEntity.getUserId()), PointType.PAYMENT, paymentEntity.getId(), PointAmount.PAYMENT);

        return new Payment(
                paymentEntity.getId(),
                paymentEntity.getUserId(),
                paymentEntity.getOrderId(),
                paymentEntity.getOriginAmount(),
                paymentEntity.getOwnedCouponId(),
                paymentEntity.getCouponDiscount(),
                paymentEntity.getUsedPoint(),
                paymentEntity.getPaidAmount(),
                paymentEntity.getState(),
                paymentEntity.getExternalPaymentKey(),
                paymentEntity.getMethod(),
                paymentEntity.getApproveCode(),
                paymentEntity.getPaidAt()
        );
    }

    public void validateForApprove(String orderKey, BigDecimal amount) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!Objects.equals(order.getUserId(), payment.getUserId())) throw new CoreException(ErrorType.NOT_FOUND_DATA);
        if (payment.getState() != PaymentState.READY) throw new CoreException(ErrorType.NOT_FOUND_DATA);
        if (payment.getPaidAmount().compareTo(amount) != 0) throw new CoreException(ErrorType.PAYMENT_AMOUNT_MISMATCH);
    }

    public Payment validateForFail(String orderKey) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        PaymentEntity paymentEntity = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        return new Payment(
                paymentEntity.getId(),
                paymentEntity.getUserId(),
                paymentEntity.getOrderId(),
                paymentEntity.getOriginAmount(),
                paymentEntity.getOwnedCouponId(),
                paymentEntity.getCouponDiscount(),
                paymentEntity.getUsedPoint(),
                paymentEntity.getPaidAmount(),
                paymentEntity.getState(),
                paymentEntity.getExternalPaymentKey(),
                paymentEntity.getMethod(),
                paymentEntity.getApproveCode(),
                paymentEntity.getPaidAt()
        );
    }
}
