package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.assembler.PaymentAssembler;
import io.april2nd.commerce.core.api.controller.v1.request.CreatePaymentRequest;
import io.april2nd.commerce.core.api.controller.v1.response.CreatePaymentResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentAssembler paymentAssembler;

    @PostMapping("/v1/payments")
    ApiResponse<CreatePaymentResponse> create(
            User user,
            @RequestBody CreatePaymentRequest request) {

        Long createdId = paymentAssembler.create(user, request);

        return ApiResponse.success(new CreatePaymentResponse(createdId));
    }

    @PostMapping("/v1/payments/callback/success")
    ApiResponse<Void> callbackForSuccess(
            @RequestParam String orderId,
            @RequestParam String paymentKey,
            @RequestParam BigDecimal amount) {
        paymentAssembler.success(orderId, paymentKey, amount);
        return ApiResponse.success();
    }

    @PostMapping("/v1/payments/callback/fail")
    ApiResponse<Void> callbackForFail(
            @RequestParam String orderId,
            @RequestParam String code,
            @RequestParam String message) {
        paymentAssembler.fail(orderId, code, message);
        return ApiResponse.success();
    }
}
