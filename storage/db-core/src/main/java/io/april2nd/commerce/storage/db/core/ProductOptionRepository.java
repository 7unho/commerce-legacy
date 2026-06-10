package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOptionEntity, Long> {
    List<ProductOptionEntity> findByProductIdAndStatusOrderByPriorityAsc(Long productId, EntityStatus status);
    List<ProductOptionEntity> findByIdInAndStatus(List<Long> productids, EntityStatus status);
}
