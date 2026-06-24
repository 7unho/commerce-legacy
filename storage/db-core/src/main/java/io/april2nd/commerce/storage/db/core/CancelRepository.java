package io.april2nd.commerce.storage.db.core;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface CancelRepository extends JpaRepository<CancelEntity, Long> {
    Long countByOrderId(Long orderId);

    @Query(
            """
            SELECT new io.april2nd.commerce.storage.db.core.SettlementCancelTarget(
                cancel.id,
                cancel.type,
                cancel.orderId,
                cancel.orderItemId,
                cancel.canceledQuantity
            )
            FROM CancelEntity cancel
            WHERE cancel.canceledAt BETWEEN :from AND :to
            """
    )
    Slice<SettlementCancelTarget> findSettlementTargetsByCanceledAtBetween(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}
