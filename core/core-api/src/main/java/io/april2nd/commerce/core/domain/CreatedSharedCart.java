package io.april2nd.commerce.core.domain;

import java.time.LocalDateTime;

public record CreatedSharedCart(
        Long cartId,
        String shareToken,
        LocalDateTime expiredAt
) {
    public String sharePath() {
        return "/shared-carts/" + shareToken;
    }
}
