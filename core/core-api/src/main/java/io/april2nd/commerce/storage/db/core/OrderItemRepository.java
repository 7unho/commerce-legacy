package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderId(Long orderId);
    List<OrderItemEntity> findByOrderIdIn(Collection<Long> orderId);

    @Query(
            """
            SELECT item FROM OrderItemEntity item
                JOIN OrderEntity orderEntity ON item.orderId = orderEntity.id
            WHERE orderEntity.userId = :userId
                AND orderEntity.state = :state
                AND orderEntity.status = :status
                AND orderEntity.createdAt >= :fromDate
                AND item.productId = :productId
                AND item.status = :status
            """
    )
    List<OrderItemEntity> findRecentOrderItemsForProduct(
            Long userId,
            Long productId,
            OrderState state,
            LocalDateTime fromDate,
            EntityStatus status
    );
}
