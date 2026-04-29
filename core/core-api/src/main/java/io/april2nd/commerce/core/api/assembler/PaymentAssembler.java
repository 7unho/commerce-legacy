package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.request.CreatePaymentRequest;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentAssembler {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final OwnedCouponService ownedCouponService;
    private final PointService pointService;

    public Long create(User user, CreatePaymentRequest request) {
        Order order = orderService.getOrder(user, request.orderKey(), OrderState.CREATED);
        List<OwnedCoupon> ownedCoupons = ownedCouponService.getOwnedCouponsForCheckout(
                user,
                order.items().stream()
                        .map(OrderItem::productId)
                        .collect(Collectors.toList())
        );
        PointBalance pointBalance = pointService.balance(user);
        
        return paymentService.createPayment(
                order,
                request.toPaymentDiscount(ownedCoupons, pointBalance)
        );
    }

    public void success(String orderId, String paymentKey, BigDecimal amount) {
        paymentService.success(orderId, paymentKey, amount);
    }

    public void fail(String orderId, String code, String message) {
        paymentService.fail(orderId, code, message);
    }
}
