package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.request.ApplyFavoriteRequest;
import io.april2nd.commerce.core.api.controller.v1.response.FavoriteResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.FavoriteTargetType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FavoriteAssembler {
    private final FavoriteService favoriteService;
    private final ProductService productService;
    private final BrandService brandService;
    private final MerchantService merchantService;

    public Page<FavoriteResponse> findFavorites(User user, FavoriteTargetType targetType, OffsetLimit offsetLimit) {
        Page<Favorite> page = favoriteService.findFavorites(user, targetType, offsetLimit);
        if (page.content().isEmpty()) {
            return new Page<>(Collections.emptyList(), page.hasNext());
        }

        List<Long> targetIds = page.content().stream()
                .map(Favorite::targetId)
                .distinct().toList();

        return switch (targetType) {
            case PRODUCT -> {
                Map<Long, Product> productMap = productService.findProducts(targetIds).stream()
                        .collect(Collectors.toMap(Product::id, Function.identity()));
                yield new Page<>(FavoriteResponse.of(page.content(), productMap), page.hasNext());
            }
            case BRAND -> {
                Map<Long, Brand> brandMap = brandService.findBrands(targetIds).stream()
                        .collect(Collectors.toMap(Brand::id, Function.identity()));
                yield new Page<>(FavoriteResponse.ofBrands(page.content(), brandMap), page.hasNext());
            }
            case MERCHANT -> {
                Map<Long, Merchant> merchantMap = merchantService.findMerchants(targetIds).stream()
                        .collect(Collectors.toMap(Merchant::id, Function.identity()));
                yield new Page<>(FavoriteResponse.ofMerchants(page.content(), merchantMap), page.hasNext());
            }
        };
    }

    public void applyFavorite(User user, ApplyFavoriteRequest request) {
        switch (request.type()) {
            case FAVORITE -> favoriteService.addFavorite(user, request.targetType(), request.targetId());
            case UNFAVORITE -> favoriteService.removeFavorite(user, request.targetType(), request.targetId());
        }
    }
}
