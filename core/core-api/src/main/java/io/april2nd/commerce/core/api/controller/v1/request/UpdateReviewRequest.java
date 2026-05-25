package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.ReviewContent;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.core.support.file.ImageHandle;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public record UpdateReviewRequest(
        BigDecimal rate,
        String content,
        List<Long> images,
        List<Long> deletedImageIds
) {
    public ReviewContent toContent() {
        if (rate.compareTo(BigDecimal.ZERO) <= 0) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (rate.compareTo(BigDecimal.valueOf(5.0)) > 0) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (content.isEmpty()) throw new CoreException(ErrorType.INVALID_REQUEST);

        return new ReviewContent(rate, content);
    }

    public ImageHandle toImageHandle() {
        List<Long> list = (images != null) ? images : Collections.emptyList();

        if (list.size() > 5) throw new CoreException(ErrorType.INVALID_REQUEST);

        return new ImageHandle(list, (deletedImageIds != null) ? deletedImageIds : Collections.emptyList());
    }
}
