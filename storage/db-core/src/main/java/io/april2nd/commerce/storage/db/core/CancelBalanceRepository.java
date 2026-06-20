package io.april2nd.commerce.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancelBalanceRepository extends JpaRepository<CancelBalanceEntity, Long> {
    Optional<CancelBalanceEntity> findByOrderId(Long orderId);
}
