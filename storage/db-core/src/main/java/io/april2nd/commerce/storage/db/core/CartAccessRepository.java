package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartAccessRepository extends JpaRepository<CartAccessEntity, Long> {
    Optional<CartAccessEntity> findByCartIdAndAccessUserIdAndStatus(Long cartId, Long accessUserId, EntityStatus status);
    Optional<CartAccessEntity> findByCartIdAndAccessUserId(Long cartId, Long accessUserId);
    Optional<CartAccessEntity> findByAccessKeyAndStatus(String accessKey, EntityStatus status);
    List<CartAccessEntity> findByAccessUserIdAndStatus(Long accessUserId, EntityStatus status);
    List<CartAccessEntity> findByCartIdAndStatus(Long cartId, EntityStatus status);
}
