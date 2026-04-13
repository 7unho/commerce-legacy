package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    FavoriteEntity findByUserIdAndProductId(Long userId, Long productId);

    Slice<FavoriteEntity> findByUserIdAndStatusAndUpdatedAtAfter(Long userId, EntityStatus status, LocalDateTime updatedAtAfter, Pageable pageable);
}
