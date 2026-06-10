package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {

    Optional<CartEntity> findByIdAndUserIdAndStatus(Long id, Long userId, EntityStatus status);

    Optional<CartEntity> findByIdAndTypeAndStatus(Long id, CartType type, EntityStatus status);

    Optional<CartEntity> findByIdAndStatus(Long id, EntityStatus status);

    List<CartEntity> findByUserIdAndTypeAndStatus(Long userId, CartType type, EntityStatus status);
}
