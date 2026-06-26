package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PaymentState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SettlementTargetLoader {
    private final SettlementSourceReader settlementSourceReader;
    private final SettlementTargetManager settlementTargetProcessor;

    private static final Logger log = LoggerFactory.getLogger(SettlementTargetLoader.class);

    public void loadTargets(LocalDate targetDate, LocalDateTime from, LocalDateTime to) {
        processPayment(targetDate, from, to);
        processCancel(targetDate, from, to);
    }

    private void processPayment(LocalDate targetDate, LocalDateTime from, LocalDateTime to) {
        Pageable paymentPageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "id"));
        Slice<SettlementPayment> payments;
        do {
            payments = settlementSourceReader.readPaymentsByStateAndPaidAtBetween(
                    PaymentState.SUCCESS,
                    from,
                    to,
                    paymentPageable
            );
            try {
                settlementTargetProcessor.processPayments(targetDate, payments.getContent());
            } catch (Exception e) {
                log.error("[SettlementTargetLoader.processPayment] `결제` 거래건 정산 대상 생성 중 오류 발생 offset: {}, size: {}, page: {}, error: {}", paymentPageable.getOffset(), paymentPageable.getPageSize(), paymentPageable.getPageNumber(), e.getMessage(), e);
            }
            paymentPageable = payments.nextPageable();
        } while (payments.hasNext());
    }

    private void processCancel(LocalDate targetDate, LocalDateTime from, LocalDateTime to) {
        Pageable cancelPageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "id"));
        Slice<SettlementCancel> cancels;

        do {
            cancels = settlementSourceReader.readCancelsByCanceledAtBetween(from, to, cancelPageable);
            try {
                settlementTargetProcessor.processCancels(targetDate, cancels.getContent());
            } catch (Exception e) {
                log.error("[SETTLEMENT_LOAD_TARGETS] `취소` 거래건 정산 대상 생성 중 오류 발생 offset: {} size: {} page: {} error: {}", cancelPageable.getOffset(), cancelPageable.getPageSize(), cancelPageable.getPageNumber(), e.getMessage(), e);
            }
            cancelPageable = cancels.nextPageable();
        } while (cancels.hasNext());
    }
}
