package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductResponse(
        String name,
        String thumbnailUrl,
        String description,
        String shortDescription,
        BigDecimal costPrice,
        BigDecimal salesPrice,
        BigDecimal discountedPrice,
        Long favoriteCount,
        Long orderCount
) {
    public static List<ProductResponse> of(
            List<Product> products,
            Map<Long, Long> favoriteCounts,
            Map<Long, Long> orderCounts
    ) {
        return products.stream()
                .map(it -> new ProductResponse(
                        it.name(),
                        it.thumbnailUrl(),
                        it.description(),
                        it.shortDescription(),
                        it.price().costPrice(),
                        it.price().salesPrice(),
                        it.price().discountedPrice(),
                        favoriteCounts.getOrDefault(it.id(), 0L),
                        orderCounts.getOrDefault(it.id(), 0L)
                ))
                .toList();
    }
}