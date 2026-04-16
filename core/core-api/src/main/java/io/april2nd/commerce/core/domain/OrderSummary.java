package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OrderState;

import java.math.BigDecimal;

public record OrderSummary(
        Long id,
        String key,
        String name,
        Long userId,
        BigDecimal totalPrice,
        OrderState state
) {}
