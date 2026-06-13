package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderInviteReader {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderInviteRepository orderInviteRepository;

    public Order getOrderByInviteKey(String inviteKey) {
        OrderInvite invite = getOrderInvite(inviteKey);
        OrderEntity orderEntity = orderRepository.findById(invite.orderId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (orderEntity.isDeleted()) throw new CoreException(ErrorType.NOT_FOUND_DATA);

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderEntity.getId());

        return new Order(
                orderEntity.getId(),
                orderEntity.getOrderKey(),
                orderEntity.getName(),
                orderEntity.getUserId(),
                orderEntity.getTotalPrice(),
                orderEntity.getState(),
                items.stream()
                        .map(it ->
                                new OrderItem(
                                        orderEntity.getId(),
                                        it.getProductId(),
                                        it.getProductOptionId(),
                                        it.getProductName(),
                                        it.getProductOptionName(),
                                        it.getThumbnailUrl(),
                                        it.getShortDescription(),
                                        it.getProductOptionDescription(),
                                        it.getQuantity(),
                                        it.getUnitPrice(),
                                        it.getTotalPrice()
                                )
                        )
                        .collect(Collectors.toList())
        );
    }

    private OrderInvite getOrderInvite(String inviteKey) {
        OrderInviteEntity found = orderInviteRepository.findByInviteKeyAndStatus(inviteKey, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        return new OrderInvite(found.getOrderId(), found.getInviteKey());
    }
}
