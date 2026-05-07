package io.april2nd.commerce.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductPolicy {
    FAVORITE_COUNT_DAYS(60),
    ORDER_COUNT_DAYS(30),
    FAVORITE_RETENTION_DAYS(30);

    private final int days;
}
