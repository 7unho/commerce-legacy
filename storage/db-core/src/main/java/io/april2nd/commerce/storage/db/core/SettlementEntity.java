package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.SettlementState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "settlement",
        indexes = {
                @Index(name = "udx_settlement_merchant", columnList = "settlementDate, merchantId", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SettlementEntity extends BaseEntity {

    private Long merchantId;
    private LocalDate settlementDate;
    private BigDecimal originalAmount;
    private BigDecimal feeAmount;
    private BigDecimal feeRate;
    private BigDecimal settlementAmount;

    @Enumerated(EnumType.STRING)
    private SettlementState state;

    // 정산 완료 처리
    public void sent() {
        this.state = SettlementState.SENT;
    }
}