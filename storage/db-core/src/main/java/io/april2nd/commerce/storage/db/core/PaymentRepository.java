package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.PaymentState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Slice<PaymentEntity> findAllByStateAndPaidAtBetween(PaymentState paymentState, LocalDateTime from, LocalDateTime to, Pageable paymentPageable);

    Optional<PaymentEntity> findByOrderId(Long orderId);
}
