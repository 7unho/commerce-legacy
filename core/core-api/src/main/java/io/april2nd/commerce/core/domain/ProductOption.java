package io.april2nd.commerce.core.domain;

public record ProductOption(
        Long id,
        Long productId,
        String name,
        String description,
        Price price
) {}
