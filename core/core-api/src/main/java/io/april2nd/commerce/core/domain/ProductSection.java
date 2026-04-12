package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.ProductSectionType;

public record ProductSection(
        ProductSectionType type,
        String content
) {}
