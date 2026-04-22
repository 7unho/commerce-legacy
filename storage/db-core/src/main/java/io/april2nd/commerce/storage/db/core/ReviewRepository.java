package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.ReviewTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByTargetTypeAndTargetId(ReviewTargetType type, Long targetId);

    Slice<ReviewEntity> findByTargetTypeAndTargetIdAndStatus(ReviewTargetType type, Long targetId, EntityStatus status, Pageable slice);

    List<ReviewEntity> findByUserIdAndReviewKeyIn(Long userId, Collection<String> reviewKey);

    Optional<ReviewEntity> findByIdAndUserId(Long reviewId, Long userId);
}
