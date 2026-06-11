package io.april2nd.commerce.core.domain;

public record NewOrderItem(
        Long productId,
        Long productOptionId,
        Long quantity
) {}
