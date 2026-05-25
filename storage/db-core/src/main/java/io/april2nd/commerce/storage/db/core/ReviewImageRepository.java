package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImageEntity, Long> {
    List<ReviewImageEntity> findByReviewIdInAndStatus(Collection<Long> reviewIds, EntityStatus status);

    List<ReviewImageEntity> findByReviewIdAndStatus(Long reviewId, EntityStatus status);
}
