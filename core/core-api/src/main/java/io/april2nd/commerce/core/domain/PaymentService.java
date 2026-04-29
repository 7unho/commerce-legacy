package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentManager paymentManager;

    public Long createPayment(Order order, PaymentDiscount paymentDiscount) {
        return paymentManager.create(order, paymentDiscount);
    }

    public Long success(String orderKey, String externalPaymentKey, BigDecimal amount) {
        return paymentManager.success(orderKey, externalPaymentKey, amount);
    }

    public void fail(String orderKey, String code, String message) {
        paymentManager.fail(orderKey, code, message);
    }
}
