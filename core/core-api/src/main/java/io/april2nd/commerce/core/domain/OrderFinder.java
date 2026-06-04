package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderFinder {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

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

    public Map<Long, Long> countOrdersByProductIds(Collection<Long> productIds, LocalDateTime from) {
        return orderItemRepository.findCountsByProductIdsAndOrderStateAndStatusAndCreatedAtAfter(
                        productIds,
                        OrderState.PAID,
                        EntityStatus.ACTIVE,
                        from
                ).stream()
                .collect(Collectors.toMap(
                        TargetCountProjection::getTargetId,
                        TargetCountProjection::getCount
                ));
    }
}
