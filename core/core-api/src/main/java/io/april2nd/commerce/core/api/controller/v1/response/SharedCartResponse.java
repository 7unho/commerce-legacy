package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.SharedCart;
import io.april2nd.commerce.core.enums.SharedCartRole;
import io.april2nd.commerce.core.enums.SharedCartState;

import java.time.LocalDateTime;
import java.util.List;

public record SharedCartResponse(
        Long cartId,
        String name,
        SharedCartRole role,
        SharedCartState status,
        LocalDateTime createdAt,
        LocalDateTime expiredAt,
        List<CartResponse.CartItemResponse> items
) {
    public static SharedCartResponse of(SharedCart cart) {
        return new SharedCartResponse(
                cart.id(), cart.name(), cart.role(), cart.state(), cart.createdAt(), cart.expiredAt(),
                cart.items().stream().map(CartResponse.CartItemResponse::of).toList()
        );
    }
}
