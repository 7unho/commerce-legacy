package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.ReviewTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Optional<ReviewEntity> findByIdAndUserId(Long reviewId, Long userId);

    List<ReviewEntity> findByUserIdAndReviewKeyIn(Long userId, Collection<String> reviewKey);

    List<ReviewEntity> findByTargetTypeAndTargetId(ReviewTargetType type, Long targetId);

    Slice<ReviewEntity> findByTargetTypeAndTargetIdAndStatus(ReviewTargetType type, Long targetId, EntityStatus status, Pageable slice);

    @Query(
            """
                SELECT DISTINCT r FROM ReviewEntity r 
                JOIN ReviewImageEntity ri ON r.id = ri.reviewId 
                WHERE r.targetType = :targetType 
                AND r.targetId = :targetId 
                AND r.status = :status 
                AND ri.status = :status
            """
    )
    Slice<ReviewEntity> findImageReviewsByTargetTypeAndTargetIdAndStatus(
            ReviewTargetType targetType,
            Long targetId,
            EntityStatus status,
            Pageable pageable
    );
}
