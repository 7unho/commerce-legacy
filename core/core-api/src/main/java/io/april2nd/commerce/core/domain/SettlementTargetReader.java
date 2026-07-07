package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.SettlementTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettlementTargetReader {
    private final SettlementTargetRepository settlementTargetRepository;

    public List<SettlementTarget> readTargets(LocalDate targetDate) {
        return settlementTargetRepository.findSummary(targetDate)
                .stream()
                .map(it ->
                        new SettlementTarget(
                                it.merchantId(),
                                it.targetAmount(),
                                it.settlementDate(),
                                it.targetCount(),
                                it.orderCount()
                        ))
                .collect(Collectors.toList());
    }
}
