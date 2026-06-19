package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.PaymentEntity;
import io.april2nd.commerce.storage.db.core.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentCreator {
    private final PaymentRepository paymentRepository;

    @Transactional
    public Long create(User payer, Order order, PaymentDiscount paymentDiscount) {
        PaymentEntity found = paymentRepository.findByOrderId(order.id()).orElse(null);

        if (found != null) {
            if (found.getState() == PaymentState.SUCCESS) throw new CoreException(ErrorType.ORDER_ALREADY_PAID);
            return found.getId();
        }

        PaymentEntity payment = PaymentEntity.builder()
                .userId(order.userId())
                .orderId(order.id())
                .originAmount(order.totalPrice())
                .ownedCouponId(paymentDiscount.getUseOwnedCouponId())
                .couponDiscount(paymentDiscount.getCouponDiscount())
                .usedPoint(paymentDiscount.getUsePoint())
                .payerId(payer.id())
                .paidAmount(paymentDiscount.paidAmount(order.totalPrice()))
                .state(PaymentState.READY)
                .build();

        return paymentRepository.save(payment).getId();
    }
}
