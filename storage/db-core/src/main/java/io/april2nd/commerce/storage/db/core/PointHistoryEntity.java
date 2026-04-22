package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.PointType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PointHistoryEntity extends BaseEntity {

    private Long userId;

    @Enumerated(EnumType.STRING)
    private PointType type;

    private Long referenceId;

    private BigDecimal amount;

    private BigDecimal balanceAfter;
}