package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.PaymentState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Slice<PaymentEntity> findAllByStateAndPaidAtBetween(PaymentState paymentState, LocalDateTime from, LocalDateTime to, Pageable paymentPageable);

    @Query(
            """
            SELECT new io.april2nd.commerce.storage.db.core.SettlementPaymentTarget(
                payment.orderId,
                payment.id
            )
            FROM PaymentEntity payment
            WHERE payment.state = :state
                AND payment.paidAt BETWEEN :from AND :to
            """
    )
    Slice<SettlementPaymentTarget> findSettlementTargetsByStateAndPaidAtBetween(
            PaymentState state,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Optional<PaymentEntity> findByOrderId(Long orderId);
}
