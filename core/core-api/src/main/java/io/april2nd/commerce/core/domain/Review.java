package io.april2nd.commerce.core.domain;

public record Review(
        Long id,
        Long userId,
        ReviewTarget target,
        ReviewContent content
) {}
