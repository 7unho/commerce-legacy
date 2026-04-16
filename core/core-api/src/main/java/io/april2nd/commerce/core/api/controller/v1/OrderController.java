package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.CreateOrderFromCartRequest;
import io.april2nd.commerce.core.api.controller.v1.request.CreateOrderRequest;
import io.april2nd.commerce.core.api.controller.v1.response.CreateOrderResponse;
import io.april2nd.commerce.core.api.controller.v1.response.OrderCheckoutResponse;
import io.april2nd.commerce.core.api.controller.v1.response.OrderListResponse;
import io.april2nd.commerce.core.api.controller.v1.response.OrderResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private OrderService orderService;
    private CartService cartService;
    private OwnedCouponService ownedCouponService;
    private PointService pointService;

    @PostMapping("/v1/orders")
    ApiResponse<CreateOrderResponse> create(
            User user,
            @RequestBody CreateOrderRequest request) {
        String key = orderService.create(user, request.toNewOrder(user));

        return ApiResponse.success(new CreateOrderResponse(key));
    }

    @PostMapping("/v1/cart-orders")
    ApiResponse<CreateOrderResponse> createFromCart(
            User user,
            @RequestBody CreateOrderFromCartRequest request) {
        Cart cart = cartService.getCart(user);
        String key = orderService.create(
                user,
                cart.toNewOrder(request.cartItemIds())
        );

        return ApiResponse.success(new CreateOrderResponse(key));
    }

    @GetMapping("/v1/orders/{orderKey}/checkout")
    ApiResponse<OrderCheckoutResponse> findOrderForCheckout(
            User user,
            @PathVariable String orderKey) {
        Order order = orderService.getOrder(user, orderKey, OrderState.CREATED);
        List<OwnedCoupon> ownedCoupons = ownedCouponService.getOwnedCouponsForCheckout(
                user,
                order.items().stream()
                        .map(OrderItem::productId)
                        .collect(Collectors.toList())
        );
        PointBalance pointBalance = pointService.balance(user);

        return ApiResponse.success(OrderCheckoutResponse.of(order, ownedCoupons, pointBalance));
    }

    @GetMapping("/v1/orders")
    ApiResponse<List<OrderListResponse>> getOrders(User user) {
        List<OrderSummary> orders = orderService.getOrders(user);
        return ApiResponse.success(OrderListResponse.of(orders));
    }

    @GetMapping("/v1/orders/{orderKey}")
    ApiResponse<OrderResponse> getOrder(
            User user,
            @PathVariable String orderKey) {
        Order order = orderService.getOrder(user, orderKey, OrderState.PAID);
        return ApiResponse.success(OrderResponse.of(order));
    }
}
