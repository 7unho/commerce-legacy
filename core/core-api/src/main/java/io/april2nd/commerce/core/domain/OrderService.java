package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
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
    private final OrderInviteReader orderInviteReader;
    private final OrderInviteManager orderInviteManager;
    private final ProductFinder productFinder;
    private final ProductOptionFinder productOptionFinder;

    public String create(User user, NewOrder newOrder) {
        List<ProductOption> productOptions = productOptionFinder.find(newOrder.productOptionIds(), EntityStatus.ACTIVE);
        List<Long> orderProductIds = productOptions.stream()
                .map(ProductOption::productId)
                .distinct()
                .collect(Collectors.toList());
        List<Product> products = productFinder.find(orderProductIds, EntityStatus.ACTIVE);

        return orderManager.create(user.id(), newOrder, products, productOptions);
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

    public String createInvite(User user, String orderKey) {
        Order order = orderReader.getOrder(user, orderKey, OrderState.CREATED);
        return orderInviteManager.create(order.id());
    }

    public Order getOrderByInviteKey(String inviteKey) {
        return orderInviteReader.getOrderByInviteKey(inviteKey);
    }
}
