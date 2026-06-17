package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentCreator paymentCreator;
    private final PaymentManager paymentManager;
    private final TransactionHistoryAppender transactionHistoryAppender;
    private final PaymentPostProcessor paymentPostProcessor;

    public Long createPayment(Order order, PaymentDiscount paymentDiscount) {
        return paymentCreator.create(order, paymentDiscount);
    }

    public Long success(String orderKey, String externalPaymentKey, BigDecimal amount) {
        paymentManager.validateForApprove(orderKey, amount);

        /**
         * NOTE: PG 승인 API 호출 => 성공 시 다음 로직으로 진행 | 실패 시 예외 발생
         */

        Payment payment = paymentManager.success(orderKey, externalPaymentKey, amount);
        transactionHistoryAppender.append(
                TransactionType.PAYMENT,
                payment.userId(),
                payment.orderId(),
                payment.id(),
                externalPaymentKey,
                payment.paidAmount(),
                "결제 성공",
                Objects.requireNonNull(payment.paidAt())
        );

        paymentPostProcessor.process(payment.userId(), orderKey);

        return payment.id();
    }

    public void fail(String orderKey, String code, String message) {
        Payment payment = paymentManager.validateForFail(orderKey);
        transactionHistoryAppender.append(
                TransactionType.PAYMENT_FAIL,
                payment.userId(),
                payment.orderId(),
                payment.id(),
                "",
                BigDecimal.valueOf(-1),
                "[%s] %s".formatted(code, message),
                LocalDateTime.now()
        );
    }
}
