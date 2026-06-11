package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderFinder orderFinder;
    private final OrderManager orderManager;
    private final OrderReader orderReader;
    private final ProductFinder productFinder;

    public String create(User user, NewOrder newOrder) {
        List<Long> orderProductIds = newOrder.items().stream()
                .map(NewOrderItem::productId)
                .collect(Collectors.toList());
        List<Product> products = productFinder.findActive(orderProductIds);

        return orderManager.create(user.id(), newOrder, products);
    }

    public Order getOrder(User user, String orderKey, OrderState state) {
        return orderReader.getOrder(user, orderKey, state);
    }

    public List<OrderSummary> getOrders(User user) {
        return orderReader.getOrders(user, OrderState.PAID);
    }

    public Map<Long, Long> recentCount(Collection<Long> productIds, LocalDateTime from) {
        return orderFinder.countOrdersByProductIds(productIds, from);
    }
}
