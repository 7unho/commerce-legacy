package io.april2nd.commerce.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewPolicy {
    ORDER_RETENTION_DAYS(14),
    EDITABLE_DAYS(7);

    private final int days;
}
