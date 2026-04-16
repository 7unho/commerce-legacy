package io.april2nd.commerce.core.api.controller.v1.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        String thumbnailUrl,
        String shortDescription,
        Long quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {}
