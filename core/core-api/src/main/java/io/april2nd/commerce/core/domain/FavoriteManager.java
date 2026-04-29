package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.FavoriteEntity;
import io.april2nd.commerce.storage.db.core.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class FavoriteManager {
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public Long add(User user, Long productId) {
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
    public Long remove(User user, Long productId) {
        FavoriteEntity existing = favoriteRepository.findByUserIdAndProductId(user.id(), productId);

        if (existing == null) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        existing.delete();
        return existing.getId();
    }
}
