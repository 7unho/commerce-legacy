package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.OrderEntity;
import io.april2nd.commerce.storage.db.core.OrderItemEntity;
import io.april2nd.commerce.storage.db.core.OrderItemRepository;
import io.april2nd.commerce.storage.db.core.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderManager {
    private final OrderKeyGenerator orderKeyGenerator;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public String create(Long userId, NewOrder newOrder, List<Product> products, List<ProductOption> productOptions) {
        if (productOptions.isEmpty()) throw new CoreException(ErrorType.NOT_FOUND_DATA);
        if (productOptions.size() != newOrder.items().stream().map(NewOrderItem::productOptionId).distinct().count()) {
            throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(
                        Product::id,
                        Function.identity()
                ));

        Map<Long, ProductOption> productOptionMap = productOptions.stream()
                .collect(Collectors.toMap(
                        ProductOption::id,
                        Function.identity()
                ));

        OrderEntity order = new OrderEntity(
                userId,
                orderKeyGenerator.generate(),
                createOrderName(newOrder.items(), productMap, productOptionMap),
                calculateTotalPrice(newOrder.items(), productOptionMap),
                OrderState.CREATED
        );

        OrderEntity saved = orderRepository.save(order);

        orderItemRepository.saveAll(
                newOrder.items().stream()
                        .map(it -> {
                            ProductOption productOption = productOptionMap.get(it.productOptionId());
                            Product product = productMap.get(productOption.productId());

                            return new OrderItemEntity(
                                    saved.getId(),
                                    product.id(),
                                    productOption.id(),
                                    product.name(),
                                    productOption.name(),
                                    product.thumbnailUrl(),
                                    product.shortDescription(),
                                    productOption.description(),
                                    it.quantity(),
                                    productOption.price().discountedPrice(),
                                    productOption.price().discountedPrice().multiply(BigDecimal.valueOf(it.quantity()))
                            );
                        })
                        .collect(Collectors.toList())
        );

        return saved.getOrderKey();
    }

    private BigDecimal calculateTotalPrice(List<NewOrderItem> items, Map<Long, ProductOption> productOptionMap) {
        return items.stream()
                .map(item -> {
                    ProductOption productOption = productOptionMap.get(item.productOptionId());

                    if (productOption == null) throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);

                    return productOption.price().discountedPrice()
                            .multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String createOrderName(
            List<NewOrderItem> items,
            Map<Long, Product> productMap,
            Map<Long, ProductOption> productOptionMap
    ) {
        NewOrderItem first = items.stream().findFirst().orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        ProductOption productOption = productOptionMap.get(first.productOptionId());
        Product product = productOption == null ? null : productMap.get(productOption.productId());

        if (product == null) throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);

        String name = product.name();

        return items.size() > 1 ? name + (" 외 " + (items.size() - 1) + "개") : name;
    }
}
