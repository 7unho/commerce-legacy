package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.request.CreateOrderFromCartRequest;
import io.april2nd.commerce.core.api.controller.v1.request.CreateOrderRequest;
import io.april2nd.commerce.core.api.controller.v1.response.OrderCheckoutResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderAssembler {
    private final OrderService orderService;
    private final CartService cartService;
    private final OwnedCouponService ownedCouponService;
    private final PointService pointService;

    public String create(User user, CreateOrderRequest request) {
        return orderService.create(user, request.toNewOrder(user));
    }

    public String createFromCart(User user, CreateOrderFromCartRequest request) {
        Cart cart = cartService.getCart(user);
        return orderService.create(
                user,
                cart.toNewOrder(request.cartItemIds())
        );
    }

    public OrderCheckoutResponse findOrderForCheckout(User user, String orderKey) {
        Order order = orderService.getOrder(user, orderKey, OrderState.CREATED);
        List<OwnedCoupon> ownedCoupons = ownedCouponService.getOwnedCouponsForCheckout(
                user,
                order.items().stream()
                        .map(OrderItem::productId)
                        .collect(Collectors.toList())
        );
        PointBalance pointBalance = pointService.balance(user);
        return OrderCheckoutResponse.of(order, ownedCoupons, pointBalance);
    }

    public List<OrderSummary> getOrders(User user) {
        return orderService.getOrders(user);
    }

    public Order getOrder(User user, String orderKey) {
        return orderService.getOrder(user, orderKey, OrderState.PAID);
    }
}
