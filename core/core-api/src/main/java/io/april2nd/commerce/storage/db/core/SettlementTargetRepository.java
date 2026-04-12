package io.april2nd.commerce.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SettlementTargetRepository extends JpaRepository<SettlementTargetEntity, Long> {
    @Query(
            """
            SELECT new io.april2nd.commerce.storage.db.core.SettlementTargetSummary(
                settlement.merchantId,
                settlement.settlementDate,
                SUM(settlement.targetAmount),
                COUNT(settlement.id),
                COUNT(DISTINCT settlement.orderId)
            )
            FROM SettlementTargetEntity settlement
                WHERE settlement.settlementDate = :settlementDate
            GROUP BY settlement.merchantId, settlement.settlementDate
            """
    )
    List<SettlementTargetSummary> findSummary(LocalDate settlementDate);
}
