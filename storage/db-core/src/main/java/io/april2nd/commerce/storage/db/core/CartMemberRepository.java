package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartMemberRepository extends JpaRepository<CartMemberEntity, Long> {
    Optional<CartMemberEntity> findByCartIdAndUserId(Long cartId, Long userId);

    List<CartMemberEntity> findByUserIdAndStatus(Long userId, EntityStatus status);

    List<CartMemberEntity> findByCartIdAndStatus(Long cartId, EntityStatus status);
}
