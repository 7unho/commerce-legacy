package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.FavoriteEntity;
import io.april2nd.commerce.storage.db.core.FavoriteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    protected FavoriteRepository favoriteRepository;

    public Page<Favorite> findFavorites(User user, OffsetLimit offsetLimit) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        Slice<FavoriteEntity> result = favoriteRepository.findByUserIdAndStatusAndUpdatedAtAfter(
                user.id(),
                EntityStatus.ACTIVE,
                cutoff,
                offsetLimit.toPageable()
        );

        return new Page<>(
                result.getContent().stream()
                        .map(it -> new Favorite(
                                it.getId(),
                                it.getUserId(),
                                it.getProductId(),
                                it.getFavoritedAt()
                        ))
                        .collect(Collectors.toList()),
                result.hasNext()
        );
    }

    @Transactional
    public Long addFavorite(User user, Long productId) {
        FavoriteEntity existing = favoriteRepository.findByUserIdAndProductId(user.id(), productId);

        if (existing != null) {
            existing.favorite();
            return existing.getId();
        }

        FavoriteEntity saved = favoriteRepository.save(
                new FavoriteEntity(
                        user.id(),
                        productId,
                        LocalDateTime.now()
                )
        );

        return saved.getId();
    }

    @Transactional
    public Long removeFavorite(User user, Long productId) {
        FavoriteEntity existing = favoriteRepository.findByUserIdAndProductId(user.id(), productId);

        if (existing == null) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        existing.delete();
        return existing.getId();
    }
}
