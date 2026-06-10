package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.CartAccess;
import io.april2nd.commerce.core.enums.CartType;

import java.time.LocalDateTime;
import java.util.List;

public record SharedCartResponse(
        String accessKey,
        Long cartId,
        CartType type,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SharedCartResponse of(CartAccess access) {
        return new SharedCartResponse(
                access.accessKey(),
                access.cartId(),
                access.type(),
                access.expiredAt(),
                access.createdAt(),
                access.updatedAt()
        );
    }

    public static List<SharedCartResponse> of(List<CartAccess> accesses) {
        return accesses.stream()
                .map(SharedCartResponse::of)
                .toList();
    }
}
