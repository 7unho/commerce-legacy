package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SettlementState;
import io.april2nd.commerce.storage.db.core.SettlementRecentAmount;
import io.april2nd.commerce.storage.db.core.SettlementRepository;
import io.april2nd.commerce.storage.db.core.SettlementTargetRepository;
import io.april2nd.commerce.storage.db.core.SettlementTargetSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettlementReader {
    private final SettlementTargetRepository settlementTargetRepository;
    private final SettlementRepository settlementRepository;

    public List<SettlementSummary> read(LocalDate settleDate) {
        List<SettlementTargetSummary> targetSummaries = settlementTargetRepository.findSummary(settleDate);
        Map<Long, SettlementRecentAmount> recentAmountsByMerchant = readRecentAmounts(settleDate, targetSummaries);

        return targetSummaries
                .stream()
                .map(summary -> new SettlementSummary(
                        summary.merchantId(),
                        summary.settlementDate(),
                        summary.targetAmount(),
                        recentAmount(recentAmountsByMerchant, summary.merchantId())
                ))
                .toList();
    }

    private Map<Long, SettlementRecentAmount> readRecentAmounts(
            LocalDate settleDate,
            List<SettlementTargetSummary> targetSummaries
    ) {
        Collection<Long> merchantIds = targetSummaries.stream()
                .map(SettlementTargetSummary::merchantId)
                .collect(Collectors.toSet());

        if (merchantIds.isEmpty()) {
            return Map.of();
        }

        return settlementRepository.findRecentAmounts(
                        merchantIds,
                        settleDate.minusMonths(1),
                        settleDate,
                        SettlementState.SENT
                )
                .stream()
                .collect(Collectors.toMap(
                        SettlementRecentAmount::merchantId,
                        Function.identity()
                ));
    }

    private BigDecimal recentAmount(Map<Long, SettlementRecentAmount> recentAmountsByMerchant, Long merchantId) {
        return Optional.ofNullable(recentAmountsByMerchant.get(merchantId))
                .map(SettlementRecentAmount::amount)
                .orElse(BigDecimal.ZERO);
    }
}
