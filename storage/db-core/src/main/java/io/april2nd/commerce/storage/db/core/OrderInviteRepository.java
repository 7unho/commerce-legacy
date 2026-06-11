package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderInviteRepository extends JpaRepository<OrderInviteEntity, Long> {
    Optional<OrderInviteEntity> findByInviteKeyAndStatus(String inviteKey, EntityStatus status);
}
