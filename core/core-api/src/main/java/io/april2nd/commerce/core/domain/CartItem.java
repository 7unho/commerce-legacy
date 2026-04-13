package io.april2nd.commerce.core.domain;

public record CartItem(
        Long id,
        Product product,
        Long quantity
) {}
