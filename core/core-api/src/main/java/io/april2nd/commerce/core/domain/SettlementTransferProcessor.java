package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.SettlementState;
import io.april2nd.commerce.storage.db.core.SettlementEntity;
import io.april2nd.commerce.storage.db.core.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettlementTransferProcessor {
    private static final Logger log = LoggerFactory.getLogger(SettlementTransferProcessor.class);

    private final SettlementRepository settlementRepository;

    @Transactional
    public int transfer() {
        List<SettlementEntity> readySettlements = settlementRepository.findByState(SettlementState.READY);
        Map<Long, SettlementEntity> settlementsById = readySettlements.stream()
                .collect(Collectors.toMap(SettlementEntity::getId, Function.identity()));
        Map<Long, List<SettlementTransferTarget>> targetsByMerchant = readySettlements.stream()
                .map(settlement -> new SettlementTransferTarget(
                        settlement.getId(),
                        settlement.getMerchantId(),
                        settlement.getSettlementAmount()
                ))
                .collect(Collectors.groupingBy(SettlementTransferTarget::merchantId));

        for (Map.Entry<Long, List<SettlementTransferTarget>> entry : targetsByMerchant.entrySet()) {
            Long merchantId = entry.getKey();
            List<SettlementTransferTarget> targets = entry.getValue();

            try {
                BigDecimal transferAmount = calculateTransferAmount(targets);

                if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn("[SETTLEMENT_TRANSFER] {} 가맹점 미정산 금액 : {} 발생 확인 요망!",
                            merchantId, transferAmount);
                    continue;
                }

                // TODO: 외부 이체 API 호출

                targets.stream()
                        .map(SettlementTransferTarget::settlementId)
                        .map(settlementsById::get)
                        .forEach(SettlementEntity::sent);

            } catch (Exception e) {
                log.error("[SETTLEMENT_TRANSFER] {} 가맹점 정산 중 에러 발생: {}",
                        merchantId, e.getMessage(), e);
            }
        }

        settlementRepository.saveAll(readySettlements);
        return targetsByMerchant.size();
    }

    private BigDecimal calculateTransferAmount(List<SettlementTransferTarget> targets) {
        return targets.stream()
                .map(SettlementTransferTarget::settlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
