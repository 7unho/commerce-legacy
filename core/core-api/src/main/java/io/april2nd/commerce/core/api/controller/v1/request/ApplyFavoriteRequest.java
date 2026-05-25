package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.enums.FavoriteTargetType;

public record ApplyFavoriteRequest(
        FavoriteTargetType targetType,
        Long targetId,
        ApplyFavoriteRequestType type
) {
    public enum ApplyFavoriteRequestType {
        FAVORITE,
        UNFAVORITE
    }
}
