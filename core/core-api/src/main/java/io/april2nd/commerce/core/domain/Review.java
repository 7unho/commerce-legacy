package io.april2nd.commerce.core.domain;

import java.util.List;

public record Review(
        Long id,
        Long userId,
        ReviewTarget target,
        ReviewContent content,
        List<ReviewImage> images
) {}
