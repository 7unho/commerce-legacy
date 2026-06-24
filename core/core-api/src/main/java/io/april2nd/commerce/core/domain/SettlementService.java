package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementTargetProcessor settlementTargetProcessor;
    private final SettlementReader settlementReader;
    private final SettlementManager settlementManager;
    private final SettlementTransferProcessor settlementTransferProcessor;

    public void loadTargets(LocalDate settleDate, LocalDateTime from, LocalDateTime to) {
        settlementTargetProcessor.load(settleDate, from, to);
    }

    public int calculate(LocalDate settleDate) {
        List<SettlementSummary> summaries = settlementReader.read(settleDate);
        return settlementManager.create(summaries);
    }

    public int transfer() {
        return settlementTransferProcessor.transfer();
    }
}
