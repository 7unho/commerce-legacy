package io.april2nd.commerce.core.domain;

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
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {}
