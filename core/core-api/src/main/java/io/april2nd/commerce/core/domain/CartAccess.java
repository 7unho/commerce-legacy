package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;

import java.time.LocalDateTime;

public record CartAccess(
        String accessKey,
        Long cartId,
        CartType type,
        Long userId,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
