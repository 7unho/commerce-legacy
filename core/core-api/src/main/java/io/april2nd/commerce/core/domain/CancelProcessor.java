package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.CancelType;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.PointType;
import io.april2nd.commerce.core.enums.TransactionType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CancelProcessor {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OwnedCouponUsageManager ownedCouponUsageManager;
    private final CancelRepository cancelRepository;
    private final CancelBalanceRepository cancelBalanceRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final PointHandler pointHandler;

    @Transactional
    public Long cancel(CancelAction action) {
        OrderEntity order = orderRepository.findByOrderKeyAndStateAndStatus(action.orderKey(), OrderState.PAID, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        CancelBalanceEntity cancelBalance = cancelBalanceRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        order.canceled();

        if (payment.hasAppliedCoupon()) {
            ownedCouponUsageManager.revert(payment.getOwnedCouponId());
        }

        User user = new User(payment.getUserId());
        pointHandler.earn(user, PointType.PAYMENT, payment.getId(), payment.getUsedPoint());
        pointHandler.deduct(user, PointType.PAYMENT, payment.getId(), PointAmount.PAYMENT);

        cancelBalance.cancel(payment.getPaidAmount(), payment.getUsedPoint(), payment.getCouponDiscount());

        CancelEntity cancel = cancelRepository.save(
                new CancelEntity(
                        CancelType.ALL,
                        payment.getUserId(),
                        payment.getOrderId(),
                        -1L,
                        payment.getId(),
                        payment.getOriginAmount(),
                        payment.getOwnedCouponId(),
                        payment.getCouponDiscount(),
                        payment.getUsedPoint(),
                        payment.getPaidAmount(),
                        -1L,
                        payment.getPaidAmount(),
                        payment.getUsedPoint(),
                        payment.getCouponDiscount(),
                        "PG_API_응답_취소_고유_값_저장",
                        LocalDateTime.now()
                )
        );

        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        TransactionType.CANCEL,
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        Objects.requireNonNull(payment.getExternalPaymentKey()),
                        payment.getPaidAmount(),
                        payment.getUsedPoint(),
                        payment.getCouponDiscount(),
                        payment.getUsedPoint(),
                        payment.getCouponDiscount(),
                        "취소 성공",
                        cancel.getCanceledAt()
                )
        );

        return cancel.getId();
    }

    @Transactional
    public Long partialCancel(PartialCancelAction action, CancelCalculated calculated) {
        OrderEntity order = orderRepository.findByOrderKeyAndStatus(action.orderKey(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        CancelBalanceEntity cancelBalance = cancelBalanceRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        OrderItemEntity targetItem = orderItemRepository.findById(action.orderItemId())
                .filter(item -> order.getId().equals(item.getOrderId()))
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        // 1. OrderItem 수량 및 상태 업데이트
        targetItem.cancel(action.quantity());

        // 2. Order 상태 업데이트
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems.stream().allMatch(OrderItemEntity::isAllCanceled)) {
            order.canceled();
        } else {
            order.partialCanceled();
        }

        // 3. 쿠폰 복원
        if (calculated.shouldRestoreCoupon()) {
            ownedCouponUsageManager.revert(payment.getOwnedCouponId());
        }

        // 4. 포인트 환불
        if (calculated.pointAmount().compareTo(BigDecimal.ZERO) > 0) {
            pointHandler.earn(new User(payment.getUserId()), PointType.PAYMENT, payment.getId(), calculated.pointAmount());
        }

        // NOTE: 한 번이라도 취소가 생기면 결제 시 지급한 포인트 회수
        if (cancelRepository.countByOrderId(order.getId()) == 0L) {
            pointHandler.deduct(new User(payment.getUserId()), PointType.PAYMENT, payment.getId(), PointAmount.PAYMENT);
        }

        // 5. 잔액 업데이트
        cancelBalance.cancel(calculated.paidAmount(), calculated.pointAmount(), calculated.couponAmount());

        CancelEntity cancel = cancelRepository.save(
                new CancelEntity(
                        CancelType.PARTIAL,
                        payment.getUserId(),
                        payment.getOrderId(),
                        action.orderItemId(),
                        payment.getId(),
                        targetItem.getUnitPrice().multiply(BigDecimal.valueOf(action.quantity())),
                        payment.getOwnedCouponId(),
                        calculated.couponAmount(),
                        calculated.pointAmount(),
                        calculated.paidAmount(),
                        action.quantity(),
                        calculated.paidAmount(),
                        calculated.pointAmount(),
                        calculated.couponAmount(),
                        "PG_API_응답_부분_취소_고유_값_저장",
                        LocalDateTime.now()
                )
        );

        transactionHistoryRepository.save(
                new TransactionHistoryEntity(
                        TransactionType.PARTIAL_CANCEL,
                        payment.getUserId(),
                        payment.getOrderId(),
                        payment.getId(),
                        Objects.requireNonNull(payment.getExternalPaymentKey()),
                        calculated.paidAmount(),
                        calculated.pointAmount(),
                        calculated.couponAmount(),
                        calculated.pointAmount(),
                        calculated.couponAmount(),
                        "부분 취소 성공",
                        cancel.getCanceledAt()
                )
        );

        return cancel.getId();
    }
}
