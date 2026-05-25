package io.april2nd.commerce.core.domain;

import java.math.BigDecimal;

public record ReviewProcessResult(
        Long id,
        ReviewFormat format
) {
    public BigDecimal pointAmount() {
        return switch (this.format) {
            case ReviewFormat.TEXT -> PointAmount.TEXT_REVIEW;
            case ReviewFormat.IMAGE -> PointAmount.IMAGE_REVIEW;
        };
    }
}
