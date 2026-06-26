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
            SELECT s.merchantId as merchantId, SUM(s.originalAmount) as amount
            FROM SettlementEntity s
            WHERE s.merchantId in :merchantIds
              AND s.settlementDate >= :startDate
              AND s.settlementDate <= :endDate
              AND s.state = :state
            GROUP BY s.merchantId
            """
    )
    List<MerchantAmountProjection> sumOriginalAmountByMerchantIdInAndSettlementDateBetweenAndState(Collection<Long> merchantIds, LocalDate startDate, LocalDate endDate, SettlementState state);
}
