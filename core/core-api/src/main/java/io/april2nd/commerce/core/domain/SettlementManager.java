package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SettlementState;
import io.april2nd.commerce.storage.db.core.SettlementEntity;
import io.april2nd.commerce.storage.db.core.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementManager {
    private final SettlementRepository settlementRepository;

    @Transactional
    public int create(List<SettlementSummary> summaries) {
        List<SettlementEntity> settlements = summaries.stream()
                .map(summary -> {
                    SettlementAmount amount = SettlementCalculator.calculate(summary.targetAmount());

                    return new SettlementEntity(
                            summary.merchantId(),
                            summary.settlementDate(),
                            amount.originalAmount(),
                            amount.feeAmount(),
                            amount.feeRate(),
                            amount.settlementAmount(),
                            SettlementState.READY
                    );
                })
                .toList();

        settlementRepository.saveAll(settlements);
        return settlements.size();
    }
}
