package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettlementTransferProcessor {
    private final SettlementTransferHandler settlementTransferHandler;

    private static final Logger log = LoggerFactory.getLogger(SettlementTransferProcessor.class);


    public void transfer(
            LocalDate targetDate,
            List<Merchant> merchants,
            List<Settlement> settlements
    ) {
        Map<Long, Merchant> merchantMap = merchants.stream()
                .collect(Collectors.toMap(
                        Merchant::id,
                        Function.identity()
                ));

        Map<Long, List<Settlement>> settlementMap = settlements.stream()
                .collect(Collectors.groupingBy(Settlement::merchantId));

        for (Map.Entry<Long, List<Settlement>> settlementGroup : settlementMap.entrySet()) {
            Long merchantId = settlementGroup.getKey();
            List<Settlement> merchantSettlements = settlementGroup.getValue();

            try {
                Merchant merchant = merchantMap.get(merchantId);

                if (merchant == null) {
                    log.warn("[SettlementTransferProcessor.transfer] {} 가맹점 정보를 찾을 수 없습니다.", merchantId);
                    continue;
                }

                if (targetDate.getDayOfMonth() % merchant.settlementCycle() != 0) {
                    log.info(
                            "[SettlementTransferProcessor.transfer] {} 가맹점은 정산 주기가 아닙니다. (cycle: {}, dayOfMonth: {})",
                            merchantId,
                            merchant.settlementCycle(),
                            targetDate.getDayOfMonth()
                    );
                    continue;
                }

                BigDecimal transferAmount = merchantSettlements.stream()
                        .map(Settlement::settlementAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn(
                            "[SettlementTransferProcessor.transfer] {} 가맹점 미정산 금액 : {} 발생 확인 요망!",
                            merchantId,
                            transferAmount
                    );
                    continue;
                }

                /*
                 * NOTE: 외부 펌 등 이체 서비스 API 호출
                 */

                settlementTransferHandler.success(merchantSettlements);
            } catch (Exception e) {
                log.error(
                        "[SettlementTransferProcessor.transfer] {} 가맹점 정산 중 에러 발생: {}",
                        merchantId,
                        e.getMessage(),
                        e
                );
            }
        }
    }
}
