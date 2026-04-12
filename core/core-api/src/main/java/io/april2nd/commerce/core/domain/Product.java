package io.april2nd.commerce.core.domain;

public record Product(
        Long id,
        String name,
        String thumbnailUrl,
        String description,
        String shortDescription,
        Price price
){}
