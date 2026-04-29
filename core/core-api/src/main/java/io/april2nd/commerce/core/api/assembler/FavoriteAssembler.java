package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.request.ApplyFavoriteRequest;
import io.april2nd.commerce.core.domain.Favorite;
import io.april2nd.commerce.core.domain.FavoriteService;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavoriteAssembler {
    private final FavoriteService favoriteService;

    public Page<Favorite> findFavorites(User user, OffsetLimit offsetLimit) {
        return favoriteService.findFavorites(user, offsetLimit);
    }

    public void applyFavorite(User user, ApplyFavoriteRequest request) {
        switch (request.type()) {
            case FAVORITE -> favoriteService.addFavorite(user, request.productId());
            case UNFAVORITE -> favoriteService.removeFavorite(user, request.productId());
        }
    }
}
