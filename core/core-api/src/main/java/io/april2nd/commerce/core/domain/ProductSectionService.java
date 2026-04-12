package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSectionService {
    private ProductFinder productFinder;


    public List<ProductSection> findSections(Long productId) {
        return productFinder.findSections(productId);
    }
}
