package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.ProductOptionEntity;
import io.april2nd.commerce.storage.db.core.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductOptionFinder {
    private final ProductOptionRepository productOptionRepository;

    public List<ProductOption> find(Long productId) {
        return productOptionRepository.findByProductId(productId).stream()
                .filter(ProductOptionEntity::isActive)
                .sorted(Comparator.comparingInt(ProductOptionEntity::getPriority))
                .map(it ->
                        new ProductOption(
                                it.getId(),
                                it.getProductId(),
                                it.getName(),
                                it.getDescription(),
                                new Price(
                                        it.getCostPrice(),
                                        it.getSalesPrice(),
                                        it.getDiscountedPrice()
                                )
                        )
                )
                .collect(Collectors.toList());
    }
}
