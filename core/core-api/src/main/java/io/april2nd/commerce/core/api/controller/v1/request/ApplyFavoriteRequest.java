package io.april2nd.commerce.core.api.controller.v1.request;

public record ApplyFavoriteRequest(
        Long productId,
        ApplyFavoriteRequestType type
) {
    public enum ApplyFavoriteRequestType {
        FAVORITE,
        UNFAVORITE
    }
}
