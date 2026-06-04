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
import java.util.Objects;
import java.util.stream.Collectors;

public record FavoriteResponse(
        Long id,
        FavoriteTargetType targetType,
        Long targetId,
        String targetName,
        String imageUrl,
        String productName,
        String productImageUrl,
        BigDecimal costPrice,
        BigDecimal salesPrice,
        BigDecimal discountedPrice,
        LocalDateTime favoritedAt,
        Boolean isChanged
) {
    public FavoriteResponse(Long id, FavoriteTargetType targetType, Long targetId, String targetName, LocalDateTime favoritedAt) {
        this(id, targetType, targetId, targetName, null, null, null, null, null, null, favoritedAt, false);
    }

    public static List<FavoriteResponse> ofProduct(List<Favorite> favorites, Map<Long, Product> productMap) {
        return favorites.stream()
                .map(it -> {
                    Product product = productMap.get(it.targetId());
                    if (product == null) return null;

                    return new FavoriteResponse(
                            it.id(),
                            FavoriteTargetType.PRODUCT,
                            product.id(),
                            product.name(),
                            product.thumbnailUrl(),
                            product.name(),
                            product.thumbnailUrl(),
                            product.price().costPrice(),
                            product.price().salesPrice(),
                            product.price().discountedPrice(),
                            it.favoritedAt(),
                            product.updatedAt().isAfter(it.favoritedAt())
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<FavoriteResponse> ofBrand(List<Favorite> favorites, Map<Long, Brand> brandMap) {
        return favorites.stream()
                .map(it -> {
                    Brand brand = brandMap.get(it.targetId());
                    if (brand == null) return null;

                    return new FavoriteResponse(
                            it.id(),
                            FavoriteTargetType.BRAND,
                            brand.id(),
                            brand.name(),
                            it.favoritedAt()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<FavoriteResponse> ofMerchant(List<Favorite> favorites, Map<Long, Merchant> merchantMap) {
        return favorites.stream()
                .map(it -> {
                    Merchant merchant = merchantMap.get(it.targetId());
                    if (merchant == null) return null;

                    return new FavoriteResponse(
                            it.id(),
                            FavoriteTargetType.MERCHANT,
                            merchant.id(),
                            merchant.name(),
                            it.favoritedAt()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }
}
