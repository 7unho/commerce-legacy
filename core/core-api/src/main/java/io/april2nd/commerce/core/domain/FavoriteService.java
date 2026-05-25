package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.FavoriteTargetType;
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

    public Page<Favorite> findFavorites(User user, FavoriteTargetType targetType, OffsetLimit offsetLimit) {
        return favoriteFinder.findFavorites(user, targetType, offsetLimit);
    }

    public Long addFavorite(User user, FavoriteTargetType targetType, Long targetId) {
        return favoriteManager.add(user, targetType, targetId);
    }

    public Long removeFavorite(User user, FavoriteTargetType targetType, Long targetId) {
        return favoriteManager.remove(user, targetType, targetId);
    }

    public Map<Long, Long> recentCount(FavoriteTargetType targetType, Collection<Long> targetIds, LocalDateTime from) {
        return favoriteFinder.countByTargetIds(targetType, targetIds, from);
    }
}
