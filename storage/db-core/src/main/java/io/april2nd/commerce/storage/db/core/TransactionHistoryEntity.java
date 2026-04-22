package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TransactionHistoryEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Long userId;
    private Long orderId;
    private Long paymentId;
    private String externalPaymentKey;
    private BigDecimal amount;
    private String message;
    private LocalDateTime occurredAt;
}