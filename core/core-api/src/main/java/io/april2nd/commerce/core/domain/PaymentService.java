package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentCreator paymentCreator;
    private final PaymentProcessor paymentProcessor;
    private final PaymentValidator paymentValidator;
    private final PaymentPostProcessor paymentPostProcessor;

    public Long createPayment(User payer, Order order, PaymentDiscount paymentDiscount) {
        return paymentCreator.create(payer, order, paymentDiscount);
    }

    public Long success(String orderKey, String externalPaymentKey, BigDecimal amount) {
        paymentValidator.validateForApprove(orderKey, amount);

        /**
         * NOTE: PG 승인 API 호출 => 성공 시 다음 로직으로 진행 | 실패 시 예외 발생
         */

        Long paymentId = paymentProcessor.success(orderKey, externalPaymentKey);
        paymentPostProcessor.process(paymentId, orderKey);

        return paymentId;
    }

    public void fail(String orderKey, String code, String message) {
        paymentValidator.validateForFail(orderKey);
        paymentProcessor.fail(orderKey, code, message);
    }
}
