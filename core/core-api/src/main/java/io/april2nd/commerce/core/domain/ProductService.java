package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductFinder productFinder;
    private final ProductOptionFinder productOptionFinder;

    public Page<Product> findProducts(Long categoryId, OffsetLimit offsetLimit) {
        return productFinder.findByCategory(categoryId, offsetLimit);
    }

    public Product findProduct(Long productId) {
        return productFinder.find(productId);
    }

    public List<Product> findProducts(List<Long> productIds) {
        return productFinder.findAll(productIds);
    }

    public List<ProductOption> findOptions(Long productId) {
        return productOptionFinder.find(productId);
    }
}
