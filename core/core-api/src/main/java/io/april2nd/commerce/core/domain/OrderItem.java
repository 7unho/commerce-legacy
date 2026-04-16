package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;

public record OrderItem(
        Long orderId,
        Long productId,
        String productName,
        String thumbnailUrl,
        String shortDescription,
        Long quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {}
