package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.PaymentState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

public interface PaymentRepository {
    Slice<PaymentEntity> findAllByStateAndPaidAtBetween(PaymentState paymentState, LocalDateTime from, LocalDateTime to, Pageable paymentPageable);
}
