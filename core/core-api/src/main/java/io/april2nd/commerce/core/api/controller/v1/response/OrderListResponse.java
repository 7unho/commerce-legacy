package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.OrderSummary;
import io.april2nd.commerce.core.enums.OrderState;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record OrderListResponse(
        String key,
        String name,
        BigDecimal totalPrice,
        OrderState state
) {
    public static OrderListResponse of(OrderSummary order) {
        return new OrderListResponse(
                order.key(),
                order.name(),
                order.totalPrice(),
                order.state()
        );
    }

    public static List<OrderListResponse> of(List<OrderSummary> orders) {
        return orders.stream()
                .map(OrderListResponse::of)
                .collect(Collectors.toList());
    }
}
