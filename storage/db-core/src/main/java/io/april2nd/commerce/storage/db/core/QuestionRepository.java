package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    Slice<QuestionEntity> findByProductIdAndStatus(Long productId, EntityStatus status, Pageable slice);

    Optional<QuestionEntity> findByIdAndUserId(Long questionId, Long id);
}
