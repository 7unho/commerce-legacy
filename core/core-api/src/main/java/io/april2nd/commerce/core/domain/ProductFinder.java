package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class ProductFinder {
    private ProductRepository productRepository;
    private ProductCategoryRepository productCategoryRepository;
    private ProductSectionRepository productSectionRepository;

    public Page<Product> findByCategory(Long categoryId, OffsetLimit offsetLimit) {
        Slice<ProductCategoryEntity> categories = productCategoryRepository.findByCategoryIdAndStatus(categoryId, EntityStatus.ACTIVE, offsetLimit.toPageable());

        List<Long> productIds = categories.getContent().stream()
                .map(ProductCategoryEntity::getProductId)
                .collect(toList());

        List<Product> products = productRepository.findAllById(productIds)
                .stream()
                .map(it -> new Product(
                        it.getId(),
                        it.getName(),
                        it.getThumbnailUrl(),
                        it.getDescription(),
                        it.getShortDescription(),
                        new Price(
                                it.getCostPrice(),
                                it.getSalesPrice(),
                                it.getDiscountedPrice()
                        )
                ))
                .collect(toList());

        return new Page(products, categories.hasNext());
    }

    public Product find(Long productId) {
        ProductEntity found = productRepository.findById(productId)
                .filter(ProductEntity::isActive)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        return new Product(
                found.getId(),
                found.getName(),
                found.getThumbnailUrl(),
                found.getDescription(),
                found.getShortDescription(),
                new Price(
                        found.getCostPrice(),
                        found.getSalesPrice(),
                        found.getDiscountedPrice()
                )
        );
    }

    public List<ProductSection> findSections(Long productId) {
        return productSectionRepository.findByProductId(productId)
                .stream()
                .filter(ProductSectionEntity::isActive)
                .map(it -> new ProductSection(it.getType(), it.getContent()))
                .collect(Collectors.toList());
    }
}
