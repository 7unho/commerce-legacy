package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.FavoriteTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    FavoriteEntity findByUserIdAndTargetTypeAndTargetId(Long userId, FavoriteTargetType targetType, Long targetId);
    Slice<FavoriteEntity> findByUserIdAndStatusAndTargetTypeAndUpdatedAtAfter(Long userId, FavoriteTargetType type, EntityStatus status, LocalDateTime from, Pageable pageable);

    @Query(
            """
            SELECT f.targetId as targetId, COUNT(f) as count
            FROM FavoriteEntity f
            WHERE f.targetType = :targetType
              AND f.targetId IN :targetIds
              AND f.status = :status
              AND f.favoritedAt >= :from
            GROUP BY f.targetId
            """
    )
    List<TargetCountProjection> countByProductIdsAndStatusAndFavoritedAtAfter(
            FavoriteTargetType targetType,
            Collection<Long> targetIds,
            EntityStatus status,
            LocalDateTime from
    );
}
