package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.Coupon;
import io.april2nd.commerce.core.domain.Product;
import io.april2nd.commerce.core.domain.ProductOption;
import io.april2nd.commerce.core.domain.ProductSection;
import io.april2nd.commerce.core.domain.RateSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record ProductDetailResponse(
        String name,
        String thumbnailUrl,
        String description,
        String shortDescription,
        BigDecimal costPrice,
        BigDecimal salesPrice,
        BigDecimal discountedPrice,
        BigDecimal rate,
        Long rateCount,
        List<ProductSectionResponse> sections,
        List<CouponResponse> coupons,
        List<ProductOptionResponse> options
) {
    public ProductDetailResponse(
            Product product,
            List<ProductSection> sections,
            RateSummary rateSummary,
            List<Coupon> coupons,
            List<ProductOption> options
    ) {
        this(
                product.name(),
                product.thumbnailUrl(),
                product.description(),
                product.shortDescription(),
                product.price().costPrice(),
                product.price().salesPrice(),
                product.price().discountedPrice(),
                rateSummary.rate(),
                rateSummary.count(),
                sections
                        .stream()
                        .map(ProductSectionResponse::of)
                        .collect(Collectors.toList()),
                coupons
                        .stream()
                        .map(CouponResponse::of)
                        .collect(Collectors.toList()),
                options
                        .stream()
                        .map(ProductOptionResponse::of)
                        .collect(Collectors.toList())
        );
    }
}
