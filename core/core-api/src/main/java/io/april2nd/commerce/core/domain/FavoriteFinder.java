package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.FavoriteTargetType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.storage.db.core.FavoriteEntity;
import io.april2nd.commerce.storage.db.core.FavoriteRepository;
import io.april2nd.commerce.storage.db.core.TargetCountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FavoriteFinder {
    private final FavoriteRepository favoriteRepository;

    public Page<Favorite> findFavorites(User user, FavoriteTargetType targetType, OffsetLimit offsetLimit) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        Slice<FavoriteEntity> result = favoriteRepository.findByUserIdAndTargetTypeAndStatusAndUpdatedAtAfter(
                user.id(),
                targetType,
                EntityStatus.ACTIVE,
                cutoff,
                offsetLimit.toPageable()
        );

        return new Page<>(
                result.getContent().stream()
                        .map(it -> new Favorite(
                                it.getId(),
                                it.getUserId(),
                                it.getTargetType(),
                                it.getTargetId(),
                                it.getFavoritedAt()
                        ))
                        .collect(Collectors.toList()),
                result.hasNext()
        );
    }

    public Map<Long, Long> countByTargetIds(FavoriteTargetType targetType, Collection<Long> targetIds, LocalDateTime from) {
        return favoriteRepository.findCountsByTargetTypeAndTargetIdsAndStatusAndFavoritedAtAfter(
                        targetType,
                        targetIds,
                        EntityStatus.ACTIVE,
                        from
                ).stream()
                .collect(Collectors.toMap(
                        TargetCountProjection::getTargetId,
                        TargetCountProjection::getCount
                ));
    }
}
