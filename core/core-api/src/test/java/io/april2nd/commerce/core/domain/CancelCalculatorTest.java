package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CouponType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.storage.db.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CancelCalculatorTest {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CancelBalanceRepository cancelBalanceRepository;
    private final OwnedCouponRepository ownedCouponRepository;
    private final CouponRepository couponRepository;
    private final CancelCalculator cancelCalculator;

    CancelCalculatorTest(
            @Mock OrderRepository orderRepository,
            @Mock OrderItemRepository orderItemRepository,
            @Mock PaymentRepository paymentRepository,
            @Mock CancelBalanceRepository cancelBalanceRepository,
            @Mock OwnedCouponRepository ownedCouponRepository,
            @Mock CouponRepository couponRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.cancelBalanceRepository = cancelBalanceRepository;
        this.ownedCouponRepository = ownedCouponRepository;
        this.couponRepository = couponRepository;
        this.cancelCalculator = new CancelCalculator(
                orderRepository,
                orderItemRepository,
                paymentRepository,
                cancelBalanceRepository,
                ownedCouponRepository,
                couponRepository
        );
    }

    @Test
    void calculatesPartialCancelForPointPayment() {
        Scenario scenario = prepareScenario(
                "Case1 - Point 결제",
                amount("30000"),
                amount("15000"),
                amount("0"),
                amount("15000"),
                amount("0"),
                3
        );

        CancelCalculateResult first = calculateAndRecord(scenario, 1L);
        assertThat(first.paidAmount()).isEqualByComparingTo("10000");
        assertThat(first.pointAmount()).isEqualByComparingTo("0");
        assertThat(first.shouldRestoreCoupon()).isFalse();

        CancelCalculateResult second = calculateAndRecord(scenario, 2L);
        assertThat(second.paidAmount()).isEqualByComparingTo("5000");
        assertThat(second.pointAmount()).isEqualByComparingTo("5000");
        assertThat(second.shouldRestoreCoupon()).isFalse();

        CancelCalculateResult third = calculateAndRecord(scenario, 3L);
        assertThat(third.paidAmount()).isEqualByComparingTo("0");
        assertThat(third.pointAmount()).isEqualByComparingTo("10000");
        assertThat(third.shouldRestoreCoupon()).isFalse();
    }

    @Test
    void calculatesPartialCancelForCouponPayment() {
        Scenario scenario = prepareScenario(
                "Case2 - 쿠폰 결제",
                amount("50000"),
                amount("40000"),
                amount("10000"),
                amount("0"),
                amount("35000"),
                5
        );

        CancelCalculateResult first = calculateAndRecord(scenario, 1L);
        assertThat(first.paidAmount()).isEqualByComparingTo("10000");
        assertThat(first.couponAmount()).isEqualByComparingTo("0");
        assertThat(first.shouldRestoreCoupon()).isFalse();

        CancelCalculateResult second = calculateAndRecord(scenario, 2L);
        assertThat(second.paidAmount()).isEqualByComparingTo("0");
        assertThat(second.couponAmount()).isEqualByComparingTo("10000");
        assertThat(second.shouldRestoreCoupon()).isTrue();

        CancelCalculateResult third = calculateAndRecord(scenario, 3L);
        assertThat(third.paidAmount()).isEqualByComparingTo("10000");
        assertThat(third.couponAmount()).isEqualByComparingTo("0");
        assertThat(third.shouldRestoreCoupon()).isFalse();

        CancelCalculateResult fourth = calculateAndRecord(scenario, 4L);
        assertThat(fourth.paidAmount()).isEqualByComparingTo("10000");

        CancelCalculateResult fifth = calculateAndRecord(scenario, 5L);
        assertThat(fifth.paidAmount()).isEqualByComparingTo("10000");
    }

    @Test
    void calculatesPartialCancelForCouponAndPointPayment() {
        Scenario scenario = prepareScenario(
                "Case3 - 쿠폰 + 포인트 결제",
                amount("60000"),
                amount("30000"),
                amount("15000"),
                amount("15000"),
                amount("35000"),
                6
        );

        CancelCalculateResult first = calculateAndRecord(scenario, 1L);
        assertThat(first.paidAmount()).isEqualByComparingTo("10000");
        assertThat(first.couponAmount()).isEqualByComparingTo("0");
        assertThat(first.pointAmount()).isEqualByComparingTo("0");

        CancelCalculateResult second = calculateAndRecord(scenario, 2L);
        assertThat(second.paidAmount()).isEqualByComparingTo("10000");
        assertThat(second.couponAmount()).isEqualByComparingTo("0");
        assertThat(second.pointAmount()).isEqualByComparingTo("0");

        CancelCalculateResult third = calculateAndRecord(scenario, 3L);
        assertThat(third.paidAmount()).isEqualByComparingTo("0");
        assertThat(third.couponAmount()).isEqualByComparingTo("10000");
        assertThat(third.pointAmount()).isEqualByComparingTo("0");
        assertThat(third.shouldRestoreCoupon()).isTrue();

        CancelCalculateResult fourth = calculateAndRecord(scenario, 4L);
        assertThat(fourth.paidAmount()).isEqualByComparingTo("5000");
        assertThat(fourth.couponAmount()).isEqualByComparingTo("5000");
        assertThat(fourth.pointAmount()).isEqualByComparingTo("0");
        assertThat(fourth.shouldRestoreCoupon()).isTrue();

        CancelCalculateResult fifth = calculateAndRecord(scenario, 5L);
        assertThat(fifth.paidAmount()).isEqualByComparingTo("5000");
        assertThat(fifth.couponAmount()).isEqualByComparingTo("0");
        assertThat(fifth.pointAmount()).isEqualByComparingTo("5000");

        CancelCalculateResult sixth = calculateAndRecord(scenario, 6L);
        assertThat(sixth.paidAmount()).isEqualByComparingTo("0");
        assertThat(sixth.couponAmount()).isEqualByComparingTo("0");
        assertThat(sixth.pointAmount()).isEqualByComparingTo("10000");
    }

    private Scenario prepareScenario(
            String name,
            BigDecimal orderAmount,
            BigDecimal paidAmount,
            BigDecimal couponDiscount,
            BigDecimal usedPoint,
            BigDecimal couponMinimumOrderAmount,
            int itemCount
    ) {
        Long orderId = 10L;
        Long paymentId = 20L;
        Long ownedCouponId = couponDiscount.compareTo(BigDecimal.ZERO) > 0 ? 30L : null;
        OrderEntity order = entityWithId(
                new OrderEntity(1L, "order-key", "주문", orderAmount, OrderState.PAID),
                orderId
        );
        PaymentEntity payment = entityWithId(
                PaymentEntity.builder()
                        .userId(1L)
                        .orderId(orderId)
                        .originAmount(orderAmount)
                        .ownedCouponId(ownedCouponId)
                        .couponDiscount(couponDiscount)
                        .usedPoint(usedPoint)
                        .payerId(1L)
                        .paidAmount(paidAmount)
                        .state(PaymentState.SUCCESS)
                        .build(),
                paymentId
        );
        CancelBalanceEntity balance = entityWithId(
                new CancelBalanceEntity(orderId, paymentId, paidAmount, usedPoint, couponDiscount),
                50L
        );
        Map<Long, OrderItemEntity> orderItemMap = createOrderItems(orderId, itemCount);

        given(orderRepository.findByOrderKeyAndStateAndStatus("order-key", OrderState.PAID, EntityStatus.ACTIVE))
                .willReturn(Optional.of(order));
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));
        given(cancelBalanceRepository.findByOrderId(orderId)).willReturn(Optional.of(balance));
        given(orderItemRepository.findById(org.mockito.ArgumentMatchers.anyLong()))
                .willAnswer(invocation -> Optional.ofNullable(orderItemMap.get(invocation.getArgument(0))));

        if (ownedCouponId != null) {
            OwnedCouponEntity ownedCoupon = entityWithId(
                    OwnedCouponEntity.builder()
                            .userId(1L)
                            .couponId(40L)
                            .maxUseCount(1L)
                            .usedCount(1L)
                            .build(),
                    ownedCouponId
            );
            CouponEntity coupon = entityWithId(
                    new CouponEntity("쿠폰", CouponType.FIXED_AMOUNT, couponDiscount, 1L, couponMinimumOrderAmount, LocalDateTime.MAX),
                    40L
            );
            given(ownedCouponRepository.findById(ownedCouponId)).willReturn(Optional.of(ownedCoupon));
            given(couponRepository.findById(40L)).willReturn(Optional.of(coupon));
        }

        printScenarioHeader(name, orderAmount, paidAmount, couponDiscount, usedPoint, couponMinimumOrderAmount);
        return new Scenario(name, balance);
    }

    private CancelCalculateResult calculateAndRecord(Scenario scenario, Long orderItemId) {
        CancelCalculateResult result = cancelCalculator.calculatePartial("order-key", orderItemId, 1L);
        scenario.balance().apply(result.paidAmount(), result.pointAmount(), result.couponAmount());
        printCancelResult(scenario, orderItemId, result);
        return result;
    }

    private void printScenarioHeader(
            String name,
            BigDecimal orderAmount,
            BigDecimal paidAmount,
            BigDecimal couponDiscount,
            BigDecimal usedPoint,
            BigDecimal couponMinimumOrderAmount
    ) {
        System.out.printf(
                "%n[%s]%norderAmount=%s, paidAmount=%s, couponDiscount=%s, usedPoint=%s, couponMinimumOrderAmount=%s%n",
                name,
                orderAmount,
                paidAmount,
                couponDiscount,
                usedPoint,
                couponMinimumOrderAmount
        );
    }

    private void printCancelResult(Scenario scenario, Long orderItemId, CancelCalculateResult result) {
        CancelBalanceEntity balance = scenario.balance();
        System.out.printf(
                "OrderItem-%d Cancel -> paid=%s, coupon=%s, point=%s, restoreCoupon=%s | remainPaid=%s, remainCoupon=%s, remainPoint=%s | canceledPaid=%s, canceledCoupon=%s, canceledPoint=%s%n",
                orderItemId,
                result.paidAmount(),
                result.couponAmount(),
                result.pointAmount(),
                result.shouldRestoreCoupon(),
                balance.getCancelablePaidAmount(),
                balance.getCancelableCouponAmount(),
                balance.getCancelablePointAmount(),
                balance.getCanceledPaidAmount(),
                balance.getCanceledCouponAmount(),
                balance.getCanceledPointAmount()
        );
    }

    private Map<Long, OrderItemEntity> createOrderItems(Long orderId, int itemCount) {
        return java.util.stream.LongStream.rangeClosed(1, itemCount)
                .mapToObj(id -> entityWithId(
                        new OrderItemEntity(
                                orderId,
                                id,
                                id,
                                "상품" + id,
                                "옵션" + id,
                                "thumbnail",
                                "short",
                                "description",
                                1L,
                                amount("10000"),
                                amount("10000")
                        ),
                        id
                ))
                .collect(Collectors.toMap(OrderItemEntity::getId, Function.identity()));
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private static <T extends BaseEntity> T entityWithId(T entity, Long id) {
        try {
            Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
            return entity;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record Scenario(String name, CancelBalanceEntity balance) {}
}
