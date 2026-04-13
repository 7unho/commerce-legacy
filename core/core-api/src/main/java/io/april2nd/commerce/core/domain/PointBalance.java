package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;

public record PointBalance(
        Long userId,
        BigDecimal balance
) {}
