package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;

public record Price(
        BigDecimal costPrice,
        BigDecimal salesPrice,
        BigDecimal discountedPrice
) {}
