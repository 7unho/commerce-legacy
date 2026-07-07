package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SettlementState;
import io.april2nd.commerce.storage.db.core.SettlementEntity;
import io.april2nd.commerce.storage.db.core.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SettlementGenerator {
    private final SettlementCalculator settlementCalculator;
    private final SettlementRepository settlementRepository;

    @Transactional
    public int generate(List<SettlementTarget> summaries, Map<Long, BigDecimal> recentSalesAmountMap) {
        List<SettlementEntity> settlements = summaries.stream()
                .map(summary -> {
                    SettlementAmount calculated = settlementCalculator.calculate(
                            summary.targetAmount(),
                            recentSalesAmountMap.getOrDefault(summary.merchantId(), BigDecimal.ZERO)
                    );

                    return new SettlementEntity(
                            summary.merchantId(),
                            summary.settlementDate(),
                            calculated.originalAmount(),
                            calculated.feeAmount(),
                            calculated.feeRate(),
                            calculated.settlementAmount(),
                            SettlementState.READY
                    );
                })
                .toList();

        settlementRepository.saveAll(settlements);
        return settlements.size();
    }
}
