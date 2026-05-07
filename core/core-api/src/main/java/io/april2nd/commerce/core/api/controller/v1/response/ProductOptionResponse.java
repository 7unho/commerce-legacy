package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.ProductOption;

import java.math.BigDecimal;

public record ProductOptionResponse(
        Long id,
        String name,
        String description,
        BigDecimal costPrice,
        BigDecimal salesPrice,
        BigDecimal discountedPrice
) {
    public static ProductOptionResponse of(ProductOption productOption) {
        return new ProductOptionResponse(
                productOption.id(),
                productOption.name(),
                productOption.description(),
                productOption.price().costPrice(),
                productOption.price().salesPrice(),
                productOption.price().discountedPrice()
        );
    }
}
