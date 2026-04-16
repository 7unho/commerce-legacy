package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.Order;
import io.april2nd.commerce.core.enums.OrderState;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record OrderResponse(
        String key,
        String name,
        BigDecimal totalPrice,
        OrderState state,
        List<OrderItemResponse> items
) {
    public static OrderResponse of(Order order) {
        return new OrderResponse(
                order.key(),
                order.name(),
                order.totalPrice(),
                order.state(),
                order.items().stream()
                        .map(it -> new OrderItemResponse(
                                it.productId(),
                                it.productName(),
                                it.thumbnailUrl(),
                                it.shortDescription(),
                                it.quantity(),
                                it.unitPrice(),
                                it.totalPrice()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
