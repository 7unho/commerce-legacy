package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.SharedCartSummary;
import io.april2nd.commerce.core.enums.SharedCartRole;
import io.april2nd.commerce.core.enums.SharedCartState;

import java.time.LocalDateTime;
import java.util.List;

public record SharedCartSummaryResponse(
        Long cartId,
        String name,
        SharedCartRole role,
        SharedCartState status,
        Long itemCount,
        LocalDateTime createdAt,
        LocalDateTime expiredAt,
        String sharePath
) {
    public static List<SharedCartSummaryResponse> of(List<SharedCartSummary> carts) {
        return carts.stream()
                .map(cart -> new SharedCartSummaryResponse(
                        cart.id(), cart.name(), cart.role(), cart.state(), cart.itemCount(),
                        cart.createdAt(), cart.expiredAt(), cart.sharePath()
                ))
                .toList();
    }
}
