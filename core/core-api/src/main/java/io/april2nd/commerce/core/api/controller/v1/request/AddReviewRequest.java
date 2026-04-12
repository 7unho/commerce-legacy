package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.ReviewContent;
import io.april2nd.commerce.core.domain.ReviewTarget;
import io.april2nd.commerce.core.enums.ReviewTargetType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

import java.math.BigDecimal;

public record AddReviewRequest(
        Long userId,
        ReviewTargetType targetType,
        Long targetId,
        BigDecimal rate,
        String content
) {
    public ReviewTarget toTarget() {
        return new ReviewTarget(targetType, targetId);
    }

    public ReviewContent toContent() {
        if (rate.compareTo(BigDecimal.ZERO) <= 0) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (rate.compareTo(BigDecimal.valueOf(5.0)) > 0) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (content.isEmpty()) throw new CoreException(ErrorType.INVALID_REQUEST);

        return new ReviewContent(rate, content);
    }
}
