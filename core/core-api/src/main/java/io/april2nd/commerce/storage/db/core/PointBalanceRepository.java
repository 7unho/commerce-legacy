package io.april2nd.commerce.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointBalanceRepository extends JpaRepository<PointBalanceEntity, Long> {
    Optional<PointBalanceEntity> findByUserId(Long userId);
}
