package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OrderState;

import java.math.BigDecimal;

public record OrderItem(
        Long orderId,
        Long productId,
        Long productOptionId,
        String productName,
        String productOptionName,
        String thumbnailUrl,
        String shortDescription,
        String productOptionDescription,
        Long quantity,
        Long canceledQuantity,
        OrderState state,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {}
