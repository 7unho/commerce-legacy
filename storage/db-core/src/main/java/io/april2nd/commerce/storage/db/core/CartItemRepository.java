package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findByCartIdAndStatus(Long cartId, EntityStatus status);

    List<CartItemEntity> findByCartIdInAndStatus(List<Long> cartIds, EntityStatus status);

    CartItemEntity findByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItemEntity> findByIdAndStatus(Long id, EntityStatus status);
}
