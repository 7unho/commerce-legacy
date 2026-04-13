package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.Product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        String name,
        String thumbnailUrl,
        String description,
        String shortDescription,
        BigDecimal costPrice,
        BigDecimal salesPrice,
        BigDecimal discountedPrice
) {
    public static List<ProductResponse> of(List<Product> products) {
        return products.stream()
                .map(it -> new ProductResponse(
                        it.name(),
                        it.thumbnailUrl(),
                        it.description(),
                        it.shortDescription(),
                        it.price().costPrice(),
                        it.price().salesPrice(),
                        it.price().discountedPrice()
                ))
                .toList();
    }
}