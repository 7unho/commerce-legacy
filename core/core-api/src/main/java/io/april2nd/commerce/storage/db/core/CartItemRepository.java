package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findByUserIdAndStatus(Long userId, EntityStatus status);

    CartItemEntity findByUserIdAndProductId(Long userId, Long productId);

    Optional<CartItemEntity> findByUserIdAndIdAndStatus(Long userId, Long id, EntityStatus status);
}
