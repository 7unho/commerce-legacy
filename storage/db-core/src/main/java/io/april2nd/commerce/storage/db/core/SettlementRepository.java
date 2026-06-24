package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.SettlementState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    List<SettlementEntity> findByState(SettlementState state);

    @Query(
            """
            SELECT new io.april2nd.commerce.storage.db.core.SettlementRecentAmount(
                settlement.merchantId,
                SUM(settlement.originalAmount)
            )
            FROM SettlementEntity settlement
            WHERE settlement.settlementDate >= :from
                AND settlement.settlementDate < :to
                AND settlement.merchantId IN :merchantIds
                AND settlement.state = :state
            GROUP BY settlement.merchantId
            """
    )
    List<SettlementRecentAmount> findRecentAmounts(
            Collection<Long> merchantIds,
            LocalDate from,
            LocalDate to,
            SettlementState state
    );
}
