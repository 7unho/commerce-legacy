package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.request.ApplyFavoriteRequest;
import io.april2nd.commerce.core.api.controller.v1.response.FavoriteResponse;
import io.april2nd.commerce.core.domain.*;
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

    public Page<FavoriteResponse> findFavorites(User user, OffsetLimit offsetLimit) {
        Page<Favorite> page = favoriteService.findFavorites(user, offsetLimit);
        if (page.content().isEmpty()) {
            return new Page<>(Collections.emptyList(), page.hasNext());
        }

        List<Long> productIds = page.content().stream()
                .map(Favorite::productId)
                .distinct().toList();

        Map<Long, Product> productMap = productService.findProducts(productIds).stream()
                .collect(Collectors.toMap(Product::id, Function.identity()));

        return new Page<>(FavoriteResponse.of(page.content(), productMap), page.hasNext());
    }

    public void applyFavorite(User user, ApplyFavoriteRequest request) {
        switch (request.type()) {
            case FAVORITE -> favoriteService.addFavorite(user, request.productId());
            case UNFAVORITE -> favoriteService.removeFavorite(user, request.productId());
        }
    }
}
