package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.CreatePaymentRequest;
import io.april2nd.commerce.core.api.controller.v1.response.CreatePaymentResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private PaymentService paymentService;
    private OrderService orderService;
    private OwnedCouponService ownedCouponService;
    private PointService pointService;

    @PostMapping("/v1/payments")
    ApiResponse<CreatePaymentResponse> create(
            User user,
            @RequestBody CreatePaymentRequest request) {
        Order order = orderService.getOrder(user, request.orderKey(), OrderState.CREATED);
        List<OwnedCoupon> ownedCoupons = ownedCouponService.getOwnedCouponsForCheckout(
                user,
                order.items().stream()
                        .map(OrderItem::productId)
                        .collect(Collectors.toList())
        );
        PointBalance pointBalance = pointService.balance(user);
        Long createdId = paymentService.createPayment(
                order,
                request.toPaymentDiscount(ownedCoupons, pointBalance)
        );

        return ApiResponse.success(new CreatePaymentResponse(createdId));
    }

    @PostMapping("/v1/payments/callback/success")
    ApiResponse<Void> callbackForSuccess(
            @RequestParam String orderId,
            @RequestParam String paymentKey,
            @RequestParam BigDecimal amount) {
        paymentService.success(orderId, paymentKey, amount);
        return ApiResponse.success();
    }

    @PostMapping("/v1/payments/callback/fail")
    ApiResponse<Void> callbackForFail(
            @RequestParam String orderId,
            @RequestParam String code,
            @RequestParam String message) {
        paymentService.fail(orderId, code, message);
        return ApiResponse.success();
    }
}
