package io.april2nd.commerce.storage.db.core;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface CancelRepository extends JpaRepository<CancelEntity, Long> {
    Long countByOrderId(Long orderId);
    Slice<CancelEntity> findAllByCanceledAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
