package io.april2nd.commerce.core.domain;

import java.time.LocalDateTime;

public record Favorite(
        Long id,
        Long userId,
        Long productId,
        LocalDateTime favoritedAt
) {}
