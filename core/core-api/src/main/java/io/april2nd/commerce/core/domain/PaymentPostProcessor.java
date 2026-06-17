package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentPostProcessor {
    private final CartItemManager cartItemManager;
    private final OrderReader orderReader;

    private static final Logger log = LoggerFactory.getLogger(PaymentPostProcessor.class);

    @Async
    public void process(Long userId, String orderKey) {
        try {
            log.info("[PAYMENT_POST_PROCESSOR.process] Start processing for usedId={}, orderKey={}", userId, orderKey);
            Order order = orderReader.getOrder(userId, orderKey, OrderState.PAID);
            List<Long> productOptionIds = order.items().stream()
                    .map(OrderItem::productOptionId)
                    .collect(Collectors.toList());

            cartItemManager.deleteItemsByProductOptions(userId, productOptionIds);
            log.info("[PAYMENT_POST_PROCESSOR.process] Successfully removed cart items for usedId={}, orderKey={}", userId, orderKey);
        } catch (Exception e) {
            log.error("[PAYMENT_POST_PROCESSOR.process] Error processing for usedId={}, orderKey={}", userId, orderKey, e);
        }
    }
}
