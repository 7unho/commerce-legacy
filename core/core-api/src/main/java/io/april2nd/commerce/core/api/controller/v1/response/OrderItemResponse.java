package io.april2nd.commerce.core.api.controller.v1.response;

import java.math.BigDecimal;

public record OrderItemResponse(
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
