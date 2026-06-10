package io.april2nd.commerce.core.domain;

public record AddCartItem(
        Long cartId,
        Long productId,
        Long productOptionId,
        Long quantity
) {}
