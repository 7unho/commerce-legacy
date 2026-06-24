package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.SettlementTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementReader {
    private final SettlementTargetRepository settlementTargetRepository;

    public List<SettlementSummary> read(LocalDate settleDate) {
        return settlementTargetRepository.findSummary(settleDate)
                .stream()
                .map(summary -> new SettlementSummary(
                        summary.merchantId(),
                        summary.settlementDate(),
                        summary.targetAmount()
                ))
                .toList();
    }
}
