package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "settlement_target")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementTargetEntity extends BaseEntity {

    private Long merchantId;

    private LocalDate settlementDate;

    private BigDecimal targetAmount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private Long transactionId;

    private Long orderId;

    private Long productId;

    private Long quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

}