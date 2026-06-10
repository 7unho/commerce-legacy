package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByOwnerIdAndTypeAndStatus(Long ownerId, CartType type, EntityStatus status);

    Optional<CartEntity> findByIdAndTypeAndStatus(Long id, CartType type, EntityStatus status);

    Optional<CartEntity> findByShareTokenAndTypeAndStatus(String shareToken, CartType type, EntityStatus status);

    List<CartEntity> findByOwnerIdAndTypeAndStatusOrderByCreatedAtDesc(Long ownerId, CartType type, EntityStatus status);

    List<CartEntity> findByIdInAndTypeAndStatus(Collection<Long> ids, CartType type, EntityStatus status);
}
