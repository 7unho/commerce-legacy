package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.TransactionType;
import io.april2nd.commerce.storage.db.core.TransactionHistoryEntity;
import io.april2nd.commerce.storage.db.core.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionHistoryAppender {
    private final TransactionHistoryRepository transactionHistoryRepository;

    @Transactional
    public void append(
            TransactionType type,
            Long userId,
            Long orderId,
            Long paymentId,
            String externalPaymentKey,
            BigDecimal amount,
            String message,
            LocalDateTime occurredAt) {
        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        type,
                        userId,
                        orderId,
                        paymentId,
                        externalPaymentKey,
                        amount,
                        message,
                        occurredAt
                )
        );
    }
}
