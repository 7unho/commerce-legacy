package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderManager {
    private final OrderKeyGenerator orderKeyGenerator;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public String create(User user, NewOrder newOrder) {
        Set<Long> orderProductIds = newOrder.items().stream()
                .map(NewOrderItem::productId)
                .collect(Collectors.toSet());

        Map<Long, ProductEntity> productMap = productRepository.findByIdInAndStatus(orderProductIds, EntityStatus.ACTIVE).stream()
                .collect(Collectors.toMap(
                        ProductEntity::getId,
                        Function.identity()
                ));

        if (productMap.isEmpty()) throw new CoreException(ErrorType.NOT_FOUND_DATA);
        if (!productMap.keySet().equals(orderProductIds)) throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);

        OrderEntity order = new OrderEntity(
                user.id(),
                orderKeyGenerator.generate(),
                createOrderName(newOrder.items(), productMap),
                calculateTotalPrice(newOrder.items(), productMap),
                OrderState.CREATED
        );

        OrderEntity saved = orderRepository.save(order);

        orderItemRepository.saveAll(
                newOrder.items().stream()
                        .map(it -> new OrderItemEntity())
                        .collect(Collectors.toList())
        );
        return saved.getOrderKey();
    }

    private BigDecimal calculateTotalPrice(List<NewOrderItem> items, Map<Long, ProductEntity> productMap) {
        return items.stream()
                .map(item -> {
                    ProductEntity product = productMap.get(item.productId());

                    if (product == null) throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);

                    return product.getDiscountedPrice()
                            .multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String createOrderName(List<NewOrderItem> items, Map<Long, ProductEntity> productMap) {
        NewOrderItem first = items.stream().findFirst().orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        ProductEntity product = productMap.get(first.productId());

        if (product == null) throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);

        String name = product.getName();

        return items.size() > 1 ? name + (" 외 " + (items.size() - 1) + "개") : name;
    }
}
