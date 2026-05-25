package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.response.ProductDetailResponse;
import io.april2nd.commerce.core.api.controller.v1.response.ProductResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.FavoriteTargetType;
import io.april2nd.commerce.core.enums.ReviewTargetType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductAssembler {
    private final ProductService productService;
    private final ProductSectionService productSectionService;
    private final ReviewService reviewService;
    private final CouponService couponService;
    private final FavoriteService favoriteService;
    private final OrderService orderService;

    public Page<ProductResponse> findProducts(Long categoryId, OffsetLimit offsetLimit) {
        Page<Product> productPage = productService.findProducts(categoryId, offsetLimit);
        List<Product> products = productPage.content();
        List<Long> productIds = products.stream().map(Product::id).toList();

        LocalDateTime now = LocalDateTime.now();
        Map<Long, Long> favoriteCounts = favoriteService.recentCount(FavoriteTargetType.PRODUCT, productIds, now.minusDays(ProductAssembleSpec.FAVORITE_COUNT_DAYS.getDays()));
        Map<Long, Long> orderCounts = orderService.recentCount(productIds, now.minusDays(ProductAssembleSpec.ORDER_COUNT_DAYS.getDays()));

        List<ProductResponse> responses = ProductResponse.of(products, favoriteCounts, orderCounts);
        return new Page<>(responses, productPage.hasNext());
    }

    public ProductDetailResponse findProduct(Long productId) {
        Product product = productService.findProduct(productId);
        List<ProductOption> options = productService.findOptions(productId);
        List<ProductSection> sections = productSectionService.findSections(productId);
        RateSummary rateSummary = reviewService.findRateSummary(new ReviewTarget(ReviewTargetType.PRODUCT, productId));
        List<Coupon> coupons = couponService.getCouponsForProducts(List.of(productId));

        return new ProductDetailResponse(product, sections, rateSummary, coupons, options);
    }
}
