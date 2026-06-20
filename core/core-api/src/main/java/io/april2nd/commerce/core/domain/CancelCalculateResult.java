package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;

public record CancelCalculateResult(
        BigDecimal paidAmount,
        BigDecimal couponAmount,
        BigDecimal pointAmount,
        boolean shouldRestoreCoupon
) {}
