package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.core.enums.SettlementState;
import io.april2nd.commerce.core.enums.TransactionType;
import io.april2nd.commerce.storage.db.core.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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
    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private PaymentRepository paymentRepository;
    private CancleRepository cancleRepository;
    private SettlementTargetRepository settlementTargetRepository;
    private SettlementRepository settlementRepository;
    private SettlementTargetLoader settlementTargetLoader;

    public void loadTargets(LocalDate settleDate, LocalDateTime from, LocalDateTime to) {
        Pageable paymentPageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "id"));
        Slice<PaymentEntity> payments;

        do {
            payments = paymentRepository.findAllByStateAndPaidAtBetween(PaymentState.SUCCESS, from, to, paymentPageable);

            try {
                settlementTargetLoader.process(
                        settleDate,
                        TransactionType.PAYMENT,
                        payments.getContent().stream()
                                .collect(Collectors.toMap(
                                        PaymentEntity::getOrderId,
                                        PaymentEntity::getId)
                                )
                );
            } catch (Exception e) {
                log.error("[SETTLEMENT_LOAD_TARGETS] `결제` 거래건 정산 대상 생성 중 오류 발생 offset: {} size: {} page: {} error: {}", paymentPageable.getOffset(), paymentPageable.getPageSize(), paymentPageable.getPageNumber(), e.getMessage(), e);
            }

            paymentPageable = payments.nextPageable();
        } while (payments.hasNext());

    }

    @Transactional
    public int calculate(LocalDate settleDate) {
        List<SettlementEntity> settlements = settlementTargetRepository.findSummary(settleDate)
                .stream()
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
                .collect(Collectors.toList());

        settlementRepository.saveAll(settlements);
        return settlements.size();
    }

    public int transfer() {
        Map<Long, List<SettlementEntity>> settlementsByMerchant =
                settlementRepository.findByState(SettlementState.READY)
                        .stream()
                        .collect(Collectors.groupingBy(SettlementEntity::getMerchantId));

        for (Map.Entry<Long, List<SettlementEntity>> entry : settlementsByMerchant.entrySet()) {
            Long merchantId = entry.getKey();
            List<SettlementEntity> settlements = entry.getValue();

            try {
                BigDecimal transferAmount = calculateTransferAmount(settlements);

                if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn("[SETTLEMENT_TRANSFER] {} 가맹점 미정산 금액 : {} 발생 확인 요망!",
                            merchantId, transferAmount);
                    continue;
                }

                // TODO: 외부 이체 API 호출

                settlements.forEach(SettlementEntity::sent);
                settlementRepository.saveAll(settlements);

            } catch (Exception e) {
                log.error("[SETTLEMENT_TRANSFER] {} 가맹점 정산 중 에러 발생: {}",
                        merchantId, e.getMessage(), e);
            }
        }

        return settlementsByMerchant.size();
    }

    private BigDecimal calculateTransferAmount(List<SettlementEntity> settlements) {
        return settlements.stream()
                .map(SettlementEntity::getSettlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
