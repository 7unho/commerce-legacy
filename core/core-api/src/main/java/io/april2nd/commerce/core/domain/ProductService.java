package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductFinder productFinder;

    public Page<Product> findProducts(Long categoryId, OffsetLimit offsetLimit) {
        return productFinder.findByCategory(categoryId, offsetLimit);
    }

    public Product findProduct(Long productId) {
        return productFinder.find(productId);
    }
}
