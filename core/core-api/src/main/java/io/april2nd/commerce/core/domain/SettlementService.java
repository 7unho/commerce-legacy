package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SettlementState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementTargetReader settlementTargetReader;
    private final SettlementGenerator settlementGenerator;
    private final SettlementTargetLoader settlementTargetLoader;
    private final SettlementTransferProcessor settlementTransferProcessor;
    private final SettlementReader settlementReader;
    private final MerchantFinder merchantFinder;

    public void loadTargets(LocalDate targetDate, LocalDateTime from, LocalDateTime to) {
        settlementTargetLoader.loadTargets(targetDate, from, to);
    }

    public int generate(LocalDate targetDate) {
        List<SettlementTarget> summaries = settlementTargetReader.readTargets(targetDate);
        List<Long> merchantIds = summaries.stream()
                .map(it -> it.merchantId())
                .collect(Collectors.toList());
        Map<Long, BigDecimal> recentSalesAmountMap = settlementReader.readRecentSalesAmounts(targetDate, merchantIds);
        return settlementGenerator.generate(summaries, recentSalesAmountMap);
    }

    public void transfer(LocalDate targetDate) {
        List<Settlement> readySettlements = settlementReader.readByState(SettlementState.READY);
        List<Long> merchantIds = readySettlements.stream().map(Settlement::merchantId).collect(Collectors.toList());

        List<Merchant> merchants = merchantFinder.find(merchantIds);
        settlementTransferProcessor.transfer(targetDate, merchants, readySettlements);
    }
}
