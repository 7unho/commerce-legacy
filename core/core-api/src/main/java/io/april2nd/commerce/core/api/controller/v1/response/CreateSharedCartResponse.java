package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.CreatedSharedCart;

import java.time.LocalDateTime;

public record CreateSharedCartResponse(
        Long cartId,
        String shareToken,
        String sharePath,
        LocalDateTime expiredAt
) {
    public static CreateSharedCartResponse of(CreatedSharedCart cart) {
        return new CreateSharedCartResponse(
                cart.cartId(), cart.shareToken(), cart.sharePath(), cart.expiredAt()
        );
    }
}
