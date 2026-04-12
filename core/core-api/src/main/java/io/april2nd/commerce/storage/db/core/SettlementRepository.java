package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.SettlementState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    List<SettlementEntity> findByState(SettlementState state);
}
