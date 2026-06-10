package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    Optional<CartItemEntity> findByUserIdAndIdAndStatus(Long userId, Long id, EntityStatus status);

    CartItemEntity findByCartIdAndProductIdAndProductOptionId(Long cartId, Long productId, Long productOptionId);

    List<CartItemEntity> findByCartIdAndStatus(Long cartId, EntityStatus status);

    List<CartItemEntity> findByUserIdAndProductOptionIdInAndStatus(Long userId, List<Long> productOptionIds, EntityStatus status);
}
