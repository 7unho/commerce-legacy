package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderKeyAndStateAndStatus(String orderKey, OrderState state, EntityStatus status);

    List<OrderEntity> findByUserIdAndStateAndStatusOrderByIdDesc(Long userId, OrderState state, EntityStatus status);
}
