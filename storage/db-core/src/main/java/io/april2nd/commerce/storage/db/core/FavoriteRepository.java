package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    FavoriteEntity findByUserIdAndProductId(Long userId, Long productId);

    Slice<FavoriteEntity> findByUserIdAndStatusAndUpdatedAtAfter(Long userId, EntityStatus status, LocalDateTime updatedAtAfter, Pageable pageable);

    @Query(
            """
            SELECT f.productId as productId, COUNT(f) as count
            FROM FavoriteEntity f
            WHERE f.productId IN :productIds
              AND f.status = :status
              AND f.favoritedAt >= :fromDate
            GROUP BY f.productId
            """
    )
    List<FavoriteCountProjection> findCountsByProductIdsAndStatusAndFavoritedAtAfter(
            Collection<Long> productIds,
            EntityStatus status,
            LocalDateTime fromDate
    );
}
