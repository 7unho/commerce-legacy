package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteFinder favoriteFinder;
    private final FavoriteManager favoriteManager;

    public Page<Favorite> findFavorites(User user, OffsetLimit offsetLimit) {
        return favoriteFinder.findFavorites(user, offsetLimit);
    }

    public Long addFavorite(User user, Long productId) {
        return favoriteManager.add(user, productId);
    }

    public Long removeFavorite(User user, Long productId) {
        return favoriteManager.remove(user, productId);
    }

    public Map<Long, Long> recentCount(Collection<Long> productIds, LocalDateTime from) {
        return favoriteFinder.countByProductIds(productIds, from);
    }
}
