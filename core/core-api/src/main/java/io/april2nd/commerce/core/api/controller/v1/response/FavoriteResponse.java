package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.Favorite;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record FavoriteResponse(
        Long id,
        Long productId,
        LocalDateTime favoritedAt
) {
    public static FavoriteResponse of(Favorite favorite) {
        return new FavoriteResponse(
                favorite.id(),
                favorite.productId(),
                favorite.favoritedAt()
        );
    }

    public static List<FavoriteResponse> of(List<Favorite> favorites) {
        return favorites.stream()
                .map(FavoriteResponse::of)
                .collect(Collectors.toList());
    }
}
