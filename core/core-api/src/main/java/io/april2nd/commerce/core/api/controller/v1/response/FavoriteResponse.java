package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.Brand;
import io.april2nd.commerce.core.domain.Favorite;
import io.april2nd.commerce.core.domain.Merchant;
import io.april2nd.commerce.core.domain.Product;
import io.april2nd.commerce.core.enums.FavoriteTargetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record FavoriteResponse(
        Long id,
        FavoriteTargetType targetType,
        Long targetId,
        String targetName,
        String targetImageUrl,
        BigDecimal salesPrice,
        BigDecimal discountedPrice,
        LocalDateTime favoritedAt,
        Boolean isChanged
) {
    public static FavoriteResponse of(Favorite favorite, Product product) {
        if (product == null) return of(favorite);
        return new FavoriteResponse(
                favorite.id(),
                favorite.targetType(),
                favorite.targetId(),
                product.name(),
                product.thumbnailUrl(),
                product.price().salesPrice(),
                product.price().discountedPrice(),
                favorite.favoritedAt(),
                product.updatedAt().isAfter(favorite.favoritedAt())
        );
    }

    public static FavoriteResponse of(Favorite favorite, Brand brand) {
        if (brand == null) return of(favorite);
        return new FavoriteResponse(
                favorite.id(),
                favorite.targetType(),
                favorite.targetId(),
                brand.name(),
                brand.imageUrl(),
                null,
                null,
                favorite.favoritedAt(),
                false
        );
    }

    public static FavoriteResponse of(Favorite favorite, Merchant merchant) {
        if (merchant == null) return of(favorite);
        return new FavoriteResponse(
                favorite.id(),
                favorite.targetType(),
                favorite.targetId(),
                merchant.name(),
                null,
                null,
                null,
                favorite.favoritedAt(),
                false
        );
    }

    public static FavoriteResponse of(Favorite favorite) {
        return new FavoriteResponse(
                favorite.id(),
                favorite.targetType(),
                favorite.targetId(),
                "Unknown Name", // TODO: Brand, Merchant 연동 시 수정 필요
                null,
                null,
                null,
                favorite.favoritedAt(),
                false
        );
    }

    public static List<FavoriteResponse> of(List<Favorite> favorites, Map<Long, Product> productMap) {
        return favorites.stream()
                .map(it -> of(it, productMap.get(it.targetId())))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<FavoriteResponse> ofBrands(List<Favorite> favorites, Map<Long, Brand> brandMap) {
        return favorites.stream()
                .map(it -> of(it, brandMap.get(it.targetId())))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<FavoriteResponse> ofMerchants(List<Favorite> favorites, Map<Long, Merchant> merchantMap) {
        return favorites.stream()
                .map(it -> of(it, merchantMap.get(it.targetId())))
                .collect(Collectors.toUnmodifiableList());
    }
}
