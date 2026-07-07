package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SettlementState;
import io.april2nd.commerce.storage.db.core.MerchantAmountProjection;
import io.april2nd.commerce.storage.db.core.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettlementReader {
    private final SettlementRepository settlementRepository;

    public Map<Long, BigDecimal> readRecentSalesAmounts(LocalDate settlementDate, Collection<Long> merchantIds) {
        if (merchantIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate startDate = SettlementRecentSalesPolicy.RANGE.startDate(settlementDate);
        LocalDate endDate = SettlementRecentSalesPolicy.RANGE.endDate(settlementDate);

        return settlementRepository.sumOriginalAmountByMerchantIdInAndSettlementDateBetweenAndState(
                        merchantIds,
                        startDate,
                        endDate,
                        SettlementState.SENT
                ).stream()
                .collect(Collectors.toMap(
                        MerchantAmountProjection::getMerchantId,
                        MerchantAmountProjection::getAmount
                ));
    }

    public List<Settlement> readByState(SettlementState state) {
        return settlementRepository.findByState(state)
                .stream()
                .map(it ->
                        new Settlement(
                                it.getId(),
                                it.getMerchantId(),
                                it.getSettlementDate(),
                                it.getOriginalAmount(),
                                it.getFeeAmount(),
                                it.getFeeRate(),
                                it.getSettlementAmount(),
                                it.getState()
                        )
                )
                .toList();
    }
}
