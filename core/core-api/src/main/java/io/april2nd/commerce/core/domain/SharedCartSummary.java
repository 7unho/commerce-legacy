package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SharedCartRole;
import io.april2nd.commerce.core.enums.SharedCartState;

import java.time.LocalDateTime;

public record SharedCartSummary(
        Long id,
        String name,
        SharedCartRole role,
        SharedCartState state,
        Long itemCount,
        LocalDateTime createdAt,
        LocalDateTime expiredAt,
        String shareToken
) {
    public String sharePath() {
        return role == SharedCartRole.OWNER ? "/shared-carts/" + shareToken : null;
    }
}
