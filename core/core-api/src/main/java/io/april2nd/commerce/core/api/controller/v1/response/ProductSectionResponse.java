package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.ProductSection;
import io.april2nd.commerce.core.enums.ProductSectionType;

public record ProductSectionResponse(
        ProductSectionType type,
        String content
) {
    static ProductSectionResponse of(ProductSection productSection) {
        return new ProductSectionResponse(productSection.type(), productSection.content());
    }
}
