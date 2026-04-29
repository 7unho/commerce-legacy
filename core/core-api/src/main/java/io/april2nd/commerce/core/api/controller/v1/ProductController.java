package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.assembler.ProductAssembler;
import io.april2nd.commerce.core.api.controller.v1.response.ProductDetailResponse;
import io.april2nd.commerce.core.api.controller.v1.response.ProductResponse;
import io.april2nd.commerce.core.domain.Product;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.response.ApiResponse;
import io.april2nd.commerce.core.support.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductAssembler productAssembler;

    @GetMapping("/v1/products")
    ApiResponse<PageResponse<ProductResponse>> findProducts(
            @RequestParam Long categoryId,
            @RequestParam Integer offset,
            @RequestParam Integer limit) {
        Page<Product> result = productAssembler.findProducts(categoryId, new OffsetLimit(offset, limit));
        return ApiResponse.success(new PageResponse<>(ProductResponse.of(result.content()), result.hasNext()));
    }

    @GetMapping("/v1/products/{productId}")
    ApiResponse<ProductDetailResponse> findProduct(@PathVariable Long productId) {
        return ApiResponse.success(productAssembler.findProduct(productId));
    }
}
