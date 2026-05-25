package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.Favorite;
import io.april2nd.commerce.core.domain.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record FavoriteResponse(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal salesPrice,
        BigDecimal discountedPrice,
        LocalDateTime favoritedAt
) {
    public static FavoriteResponse of(Favorite favorite, Product product) {
        return new FavoriteResponse(
                favorite.id(),
                favorite.productId(),
                product.name(),
                product.thumbnailUrl(),
                product.price().salesPrice(),
                product.price().discountedPrice(),
                favorite.favoritedAt()
        );
    }

    public static List<FavoriteResponse> of(List<Favorite> favorites, Map<Long, Product> productMap) {
        return favorites.stream()
                .map(it -> of(it, productMap.get(it.productId())))
                .collect(Collectors.toUnmodifiableList());
    }
}
