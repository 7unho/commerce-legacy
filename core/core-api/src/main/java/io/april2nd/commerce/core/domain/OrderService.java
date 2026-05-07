package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderFinder orderFinder;
    private final OrderManager orderManager;

    public String create(User user, NewOrder newOrder) {
        return orderManager.create(user, newOrder);
    }

    public Order getOrder(User user, String orderKey, OrderState state) {
        return orderFinder.getOrder(user, orderKey, state);
    }

    public List<OrderSummary> getOrders(User user) {
        return orderFinder.getOrders(user);
    }

    public Map<Long, Long> getOrderCounts(Collection<Long> productIds, Integer days) {
        return orderFinder.findOrderCounts(productIds, days);
    }
}
