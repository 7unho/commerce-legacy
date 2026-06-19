package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.PaymentEntity;
import io.april2nd.commerce.storage.db.core.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentPostProcessor {
    private final PaymentRepository paymentRepository;
    private final CartItemManager cartItemManager;
    private final OrderReader orderReader;

    private static final Logger log = LoggerFactory.getLogger(PaymentPostProcessor.class);

    @Async
    public void process(Long paymentId, String orderKey) {
        try {
            PaymentEntity payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
            if (payment.getState() != PaymentState.SUCCESS) throw new CoreException(ErrorType.PAYMENT_INVALID_STATE);

            Order order = orderReader.getOrder(payment.getUserId(), orderKey, OrderState.PAID);
            cartItemManager.deleteItemsByProductOptions(
                    payment.getUserId(),
                    order.items().stream()
                            .map(OrderItem::productOptionId)
                            .toList()
            );
        } catch (Exception e) {
            log.error("[PaymentPostProcessor.process] Error processing for paymentId={}, orderKey={}", paymentId, orderKey, e);
        }
    }
}
