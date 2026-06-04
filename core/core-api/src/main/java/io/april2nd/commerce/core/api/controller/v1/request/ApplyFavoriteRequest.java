package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.enums.FavoriteTargetType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

public record ApplyFavoriteRequest(
        FavoriteTargetType targetType,
        Long targetId,
        ApplyFavoriteRequestType type
) {
    public ApplyFavoriteRequest {
        if (type == null || targetId == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST);
        }
    }

    public enum ApplyFavoriteRequestType {
        FAVORITE,
        UNFAVORITE
    }
}
