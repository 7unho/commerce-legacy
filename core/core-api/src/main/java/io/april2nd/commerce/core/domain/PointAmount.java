package io.april2nd.commerce.core.domain;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class PointAmount {
    public static final BigDecimal TEXT_REVIEW = BigDecimal.valueOf(1000);
    public static final BigDecimal IMAGE_REVIEW = BigDecimal.valueOf(1500);
    public static final BigDecimal PAYMENT = BigDecimal.valueOf(2000);
}
