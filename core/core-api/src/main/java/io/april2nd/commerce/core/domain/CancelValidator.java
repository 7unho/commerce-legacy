package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelValidator {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public void validate(User user, CancelAction action) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(action.orderKey(), OrderState.PAID, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!order.getUserId().equals(user.id())) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (payment.getState() != PaymentState.SUCCESS) throw new CoreException(ErrorType.PAYMENT_INVALID_STATE);
    }

    public void validatePartial(User user, PartialCancelAction action) {
        OrderEntity order = orderRepository.findByOrderKeyAndStatus(action.orderKey(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!order.getUserId().equals(user.id())) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        if (order.getState() != OrderState.PAID && order.getState() != OrderState.PARTIAL_CANCELED) throw new CoreException(ErrorType.PAYMENT_INVALID_STATE);

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (payment.getState() != PaymentState.SUCCESS) throw new CoreException(ErrorType.PAYMENT_INVALID_STATE);

        OrderItemEntity targetItem = orderItemRepository.findById(action.orderItemId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!order.getId().equals(targetItem.getOrderId())) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        if (targetItem.getCancelableQuantity() < action.quantity()) throw new CoreException(ErrorType.INVALID_REQUEST);
    }
}
