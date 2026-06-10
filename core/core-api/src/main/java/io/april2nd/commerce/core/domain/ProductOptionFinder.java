package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductOptionFinder {
    private final ProductOptionRepository productOptionRepository;

    public List<ProductOption> find(Long productId) {
        return productOptionRepository.findByProductIdAndStatusOrderByPriorityAsc(productId, EntityStatus.ACTIVE).stream()
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

    public List<ProductOption> find(List<Long> ids, EntityStatus status) {
        return productOptionRepository.findByIdInAndStatus(ids, status)
                .stream()
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
