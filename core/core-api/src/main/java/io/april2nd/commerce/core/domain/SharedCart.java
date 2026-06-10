package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SharedCartRole;
import io.april2nd.commerce.core.enums.SharedCartState;

import java.time.LocalDateTime;
import java.util.List;

public record SharedCart(
        Long id,
        String name,
        SharedCartRole role,
        SharedCartState state,
        LocalDateTime createdAt,
        LocalDateTime expiredAt,
        List<CartItem> items
) {}
