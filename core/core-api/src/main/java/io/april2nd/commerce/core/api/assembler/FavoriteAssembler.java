package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.request.ApplyFavoriteRequest;
import io.april2nd.commerce.core.api.controller.v1.response.FavoriteResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.FavoriteTargetType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FavoriteAssembler {
    private final FavoriteService favoriteService;
    private final ProductService productService;
    private final BrandService brandService;
    private final MerchantService merchantService;

    public void applyFavorite(User user, ApplyFavoriteRequest request) {
        FavoriteTargetType targetType = Objects.requireNonNullElse(request.targetType(), FavoriteTargetType.PRODUCT);
        Long targetId = Optional.ofNullable(request.targetId())
                .orElseThrow(() -> new CoreException(ErrorType.INVALID_REQUEST));

        switch (request.type()) {
            case FAVORITE -> favoriteService.addFavorite(user, targetType, targetId);
            case UNFAVORITE -> favoriteService.removeFavorite(user, targetType, targetId);
        }
    }

    public Page<FavoriteResponse> getFavorites(User user, FavoriteTargetType targetType, OffsetLimit offsetLimit) {
        return switch (targetType) {
            case PRODUCT -> getProductFavorites(user, targetType, offsetLimit);
            case BRAND -> getBrandFavorites(user, targetType, offsetLimit);
            case MERCHANT -> getMerchantFavorites(user, targetType, offsetLimit);
        };
    }

    private Page<FavoriteResponse> getProductFavorites(User user, FavoriteTargetType targetType, OffsetLimit offsetLimit) {
        Page<Favorite> favorites = favoriteService.findFavorites(user, targetType, offsetLimit);
        List<Long> productIds = favorites.content().stream()
                .filter(it -> it.targetType() == FavoriteTargetType.PRODUCT)
                .map(Favorite::targetId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productService.findProducts(productIds).stream()
                .collect(Collectors.toMap(Product::id, Function.identity()));

        return new Page<>(FavoriteResponse.ofProduct(favorites.content(), productMap), favorites.hasNext());
    }

    private Page<FavoriteResponse> getBrandFavorites(User user, FavoriteTargetType targetType, OffsetLimit offsetLimit) {
        Page<Favorite> favorites = favoriteService.findFavorites(user, targetType, offsetLimit);
        List<Long> brandIds = favorites.content().stream()
                .filter(it -> it.targetType() == FavoriteTargetType.BRAND)
                .map(Favorite::targetId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Brand> brandMap = brandService.find(brandIds).stream()
                .collect(Collectors.toMap(Brand::id, Function.identity()));

        return new Page<>(FavoriteResponse.ofBrand(favorites.content(), brandMap), favorites.hasNext());
    }

    private Page<FavoriteResponse> getMerchantFavorites(User user, FavoriteTargetType targetType, OffsetLimit offsetLimit) {
        Page<Favorite> favorites = favoriteService.findFavorites(user, targetType, offsetLimit);
        List<Long> merchantIds = favorites.content().stream()
                .filter(it -> it.targetType() == FavoriteTargetType.MERCHANT)
                .map(Favorite::targetId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Merchant> merchantMap = merchantService.find(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::id, Function.identity()));

        return new Page<>(FavoriteResponse.ofMerchant(favorites.content(), merchantMap), favorites.hasNext());
    }
}
