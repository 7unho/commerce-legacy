package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.FavoriteTargetType;

import java.time.LocalDateTime;

public record Favorite(
        Long id,
        Long userId,
        FavoriteTargetType targetType,
        Long targetId,
        LocalDateTime favoritedAt
) {}
