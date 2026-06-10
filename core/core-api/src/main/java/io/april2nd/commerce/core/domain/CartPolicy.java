package io.april2nd.commerce.core.domain;

import java.time.LocalDateTime;

public enum CartPolicy {
    SHARED_EXPIRATION_DAYS(7L);

    private final long days;

    CartPolicy(long days) {
        this.days = days;
    }

    public LocalDateTime expireAt(LocalDateTime createdAt) {
        return createdAt.plusDays(days);
    }
}
