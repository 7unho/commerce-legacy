package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PointType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointHistory(
        Long id,
        Long userId,
        PointType type,
        Long referenceId,
        BigDecimal amount,
        LocalDateTime appliedAt
) {}
