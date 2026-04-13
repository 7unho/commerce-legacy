package io.april2nd.commerce.core.domain;

public record AddCartItem(
        Long productId,
        Long quantity
) {}
