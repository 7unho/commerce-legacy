package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.storage.db.core.CancelRepository;
import io.april2nd.commerce.storage.db.core.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SettlementSourceReader {
    private final PaymentRepository paymentRepository;
    private final CancelRepository cancelRepository;

    public Slice<SettlementPayment> readPaymentsByStateAndPaidAtBetween(PaymentState state, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return paymentRepository.findAllByStateAndPaidAtBetween(state, from, to, pageable)
                .map(it ->
                        new SettlementPayment(
                                it.getId(),
                                it.getUserId(),
                                it.getOrderId(),
                                it.getOriginAmount(),
                                it.getOwnedCouponId(),
                                it.getCouponDiscount(),
                                it.getUsedPoint(),
                                it.getPayerId(),
                                it.getPaidAmount(),
                                it.getState(),
                                it.getExternalPaymentKey(),
                                it.getMethod(),
                                it.getApproveCode(),
                                it.getPaidAt())
                );
    }

    public Slice<SettlementCancel> readCancelsByCanceledAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return cancelRepository.findAllByCanceledAtBetween(from, to, pageable)
                .map(it -> new SettlementCancel(
                        it.getId(),
                        it.getUserId(),
                        it.getType(),
                        it.getOrderId(),
                        it.getOrderItemId(),
                        it.getPaymentId(),
                        it.getOriginAmount(),
                        it.getOwnedCouponId(),
                        it.getCouponDiscount(),
                        it.getUsedPoint(),
                        it.getPaidAmount(),
                        it.getCanceledQuantity(),
                        it.getCanceledPaidAmount(),
                        it.getCanceledPointAmount(),
                        it.getCanceledCouponAmount(),
                        it.getExternalCancelKey(),
                        it.getCanceledAt()
                ));
    }
}
