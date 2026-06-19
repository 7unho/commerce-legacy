package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.OrderEntity;
import io.april2nd.commerce.storage.db.core.OrderRepository;
import io.april2nd.commerce.storage.db.core.PaymentEntity;
import io.april2nd.commerce.storage.db.core.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PaymentValidator {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public void validateForApprove(String orderKey, BigDecimal amount) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (payment.getState() != PaymentState.READY) throw new CoreException(ErrorType.PAYMENT_INVALID_STATE);
        if (payment.getPaidAmount().compareTo(amount) != 0) throw new CoreException(ErrorType.PAYMENT_AMOUNT_MISMATCH);
    }

    public void validateForFail(String orderKey) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, OrderState.CREATED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
    }
}
