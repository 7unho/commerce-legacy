package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.ReviewTargetType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.response.ApiResponse;
import io.april2nd.commerce.core.support.response.PageResponse;
import io.april2nd.commerce.core.api.controller.v1.response.ProductDetailResponse;
import io.april2nd.commerce.core.api.controller.v1.response.ProductResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {
    private ProductService productService;
    private ProductSectionService productSectionService;
    private ReviewService reviewService;
    private CouponService couponService;

    @GetMapping("/v1/products")
    ApiResponse<PageResponse<ProductResponse>> findProducts(
            @RequestParam Long categoryId,
            @RequestParam Integer offset,
            @RequestParam Integer limit) {
        Page<Product> result = productService.findProducts(categoryId, new OffsetLimit(offset, limit));
        return ApiResponse.success(new PageResponse<>(ProductResponse.of(result.content()), result.hasNext()));
    }

    @GetMapping("/v1/products/{productId}")
    ApiResponse<ProductDetailResponse> findProduct(@PathVariable Long productId) {
        Product product = productService.findProduct(productId);
        List<ProductSection> sections = productSectionService.findSections(productId);
        RateSummary rateSummary = reviewService.findRateSummary(new ReviewTarget(ReviewTargetType.PRODUCT, productId));

        // NOTE: 별도 API 분리 필요성
        List<Coupon> coupons = couponService.getCouponsForProducts(List.of(productId));
        return ApiResponse.success(new ProductDetailResponse(product, sections, rateSummary, coupons));
    }
}
