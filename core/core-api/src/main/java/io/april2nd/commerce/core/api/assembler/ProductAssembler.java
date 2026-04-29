package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.response.ProductDetailResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.ReviewTargetType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductAssembler {
    private final ProductService productService;
    private final ProductSectionService productSectionService;
    private final ReviewService reviewService;
    private final CouponService couponService;

    public Page<Product> findProducts(Long categoryId, OffsetLimit offsetLimit) {
        return productService.findProducts(categoryId, offsetLimit);
    }

    public ProductDetailResponse findProduct(Long productId) {
        Product product = productService.findProduct(productId);
        List<ProductSection> sections = productSectionService.findSections(productId);
        RateSummary rateSummary = reviewService.findRateSummary(new ReviewTarget(ReviewTargetType.PRODUCT, productId));
        List<Coupon> coupons = couponService.getCouponsForProducts(List.of(productId));
        
        return new ProductDetailResponse(product, sections, rateSummary, coupons);
    }
}
