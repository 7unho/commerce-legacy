package io.april2nd.commerce.core.domain;

public record PartialCancelAction(
        String orderKey,
        Long orderItemId,
        Long quantity
) {}
