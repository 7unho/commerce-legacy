package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.ReviewTargetType;

public record ReviewTarget(
        ReviewTargetType type,
        Long id
) {}
