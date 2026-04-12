package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;

public record ReviewContent(
        BigDecimal rate,
        String content
) {}
