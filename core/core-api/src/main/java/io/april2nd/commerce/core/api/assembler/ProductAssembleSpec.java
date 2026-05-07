package io.april2nd.commerce.core.api.assembler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductAssembleSpec {
    FAVORITE_COUNT_DAYS(60L),
    ORDER_COUNT_DAYS(30L),
    FAVORITE_RETENTION_DAYS(30L);

    private final long days;
}
