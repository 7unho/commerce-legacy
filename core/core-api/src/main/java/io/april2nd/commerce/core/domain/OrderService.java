package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private OrderKeyGenerator orderKeyGenerator;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private ProductRepository productRepository;

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

    @Transactional
    public Order getOrder(User user, String orderKey, OrderState state) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, state, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        if (!user.id().equals(order.getUserId())) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
        if (items.isEmpty()) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        return new Order(
                order.getId(),
                order.getOrderKey(),
                order.getName(),
                user.id(),
                order.getTotalPrice(),
                order.getState(),
                items.stream()
                        .map(it -> new OrderItem(
                                order.getId(),
                                it.getProductId(),
                                it.getProductName(),
                                it.getThumbnailUrl(),
                                it.getShortDescription(),
                                it.getQuantity(),
                                it.getUnitPrice(),
                                it.getTotalPrice()
                        ))
                        .collect(Collectors.toList())

        );
    }

    @Transactional
    public List<OrderSummary> getOrders(User user) {
        List<OrderEntity> orders = orderRepository.findByUserIdAndStateAndStatusOrderByIdDesc(user.id(), OrderState.PAID, EntityStatus.ACTIVE);
        if (orders.isEmpty()) return Collections.emptyList();

        return orders.stream()
                .map(it -> new OrderSummary(
                        it.getId(),
                        it.getOrderKey(),
                        it.getName(),
                        user.id(),
                        it.getTotalPrice(),
                        it.getState()
                ))
                .collect(Collectors.toList());
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

        if (productMap == null) throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);

        String name = product.getName();

        return items.size() > 1 ? name + (" 외 " + (items.size() - 1) + "개") : name;
    }
}
