package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CouponType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.OwnedCouponState;
import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.core.enums.TransactionType;
import io.april2nd.commerce.storage.db.core.BaseEntity;
import io.april2nd.commerce.storage.db.core.CancelBalanceEntity;
import io.april2nd.commerce.storage.db.core.CancelBalanceRepository;
import io.april2nd.commerce.storage.db.core.CancelEntity;
import io.april2nd.commerce.storage.db.core.CancelRepository;
import io.april2nd.commerce.storage.db.core.CouponEntity;
import io.april2nd.commerce.storage.db.core.CouponRepository;
import io.april2nd.commerce.storage.db.core.OrderEntity;
import io.april2nd.commerce.storage.db.core.OrderItemEntity;
import io.april2nd.commerce.storage.db.core.OrderItemRepository;
import io.april2nd.commerce.storage.db.core.OrderRepository;
import io.april2nd.commerce.storage.db.core.OwnedCouponEntity;
import io.april2nd.commerce.storage.db.core.OwnedCouponRepository;
import io.april2nd.commerce.storage.db.core.PaymentEntity;
import io.april2nd.commerce.storage.db.core.PaymentRepository;
import io.april2nd.commerce.storage.db.core.TransactionHistoryEntity;
import io.april2nd.commerce.storage.db.core.TransactionHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelService 부분 취소 통합 흐름")
class CancelServicePartialCancelTest {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CancelBalanceRepository cancelBalanceRepository;
    private final CancelRepository cancelRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final OwnedCouponRepository ownedCouponRepository;
    private final CouponRepository couponRepository;
    private final OwnedCouponUsageManager ownedCouponUsageManager;
    private final PointHandler pointHandler;
    private final CancelService cancelService;

    CancelServicePartialCancelTest(
            @Mock OrderRepository orderRepository,
            @Mock OrderItemRepository orderItemRepository,
            @Mock PaymentRepository paymentRepository,
            @Mock CancelBalanceRepository cancelBalanceRepository,
            @Mock CancelRepository cancelRepository,
            @Mock TransactionHistoryRepository transactionHistoryRepository,
            @Mock OwnedCouponRepository ownedCouponRepository,
            @Mock CouponRepository couponRepository,
            @Mock OwnedCouponUsageManager ownedCouponUsageManager,
            @Mock PointHandler pointHandler
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.cancelBalanceRepository = cancelBalanceRepository;
        this.cancelRepository = cancelRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.ownedCouponRepository = ownedCouponRepository;
        this.couponRepository = couponRepository;
        this.ownedCouponUsageManager = ownedCouponUsageManager;
        this.pointHandler = pointHandler;

        CancelValidator cancelValidator = new CancelValidator(orderRepository, orderItemRepository, paymentRepository);
        OwnedCouponReader ownedCouponReader = new OwnedCouponReader(couponRepository, ownedCouponRepository);
        CancelCalculator cancelCalculator = new CancelCalculator(orderRepository, orderItemRepository, paymentRepository, cancelBalanceRepository, ownedCouponReader);
        CancelProcessor cancelProcessor = new CancelProcessor(
                orderRepository,
                orderItemRepository,
                paymentRepository,
                ownedCouponUsageManager,
                cancelRepository,
                cancelBalanceRepository,
                transactionHistoryRepository,
                pointHandler
        );
        this.cancelService = new CancelService(cancelValidator, cancelCalculator, cancelProcessor);
    }

    @Test
    @DisplayName("Case1 - 포인트 결제 주문을 OrderItem 단위로 부분 취소한다")
    void partiallyCancelsPointPayment() {
        Scenario scenario = prepareScenario(
                amount("30000"),
                amount("15000"),
                amount("0"),
                amount("15000"),
                amount("0"),
                3
        );

        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 1L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 2L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 3L, 1L));

        assertCancelRecords(
                List.of(
                        expected("10000", "0", "0"),
                        expected("5000", "0", "5000"),
                        expected("0", "0", "10000")
                )
        );
        assertTransactionHistories(
                List.of(
                        expected("10000", "0", "0"),
                        expected("5000", "0", "5000"),
                        expected("0", "0", "10000")
                )
        );
        assertBalance(scenario.balance(), "0", "0", "0", "15000", "0", "15000");
        assertOrderItemsCanceled(scenario.orderItemMap());
    }

    @Test
    @DisplayName("Case2 - 쿠폰 결제 주문에서 최소 주문 금액 미달 시 쿠폰을 복원하며 부분 취소한다")
    void partiallyCancelsCouponPayment() {
        Scenario scenario = prepareScenario(
                amount("50000"),
                amount("40000"),
                amount("10000"),
                amount("0"),
                amount("35000"),
                5
        );

        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 1L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 2L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 3L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 4L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 5L, 1L));

        assertCancelRecords(
                List.of(
                        expected("10000", "0", "0"),
                        expected("0", "10000", "0"),
                        expected("10000", "0", "0"),
                        expected("10000", "0", "0"),
                        expected("10000", "0", "0")
                )
        );
        assertTransactionHistories(
                List.of(
                        expected("10000", "0", "0"),
                        expected("0", "10000", "0"),
                        expected("10000", "0", "0"),
                        expected("10000", "0", "0"),
                        expected("10000", "0", "0")
                )
        );
        assertBalance(scenario.balance(), "0", "0", "0", "40000", "10000", "0");
        assertOrderItemsCanceled(scenario.orderItemMap());
    }

    @Test
    @DisplayName("Case3 - 쿠폰과 포인트를 함께 사용한 주문을 결제금액, 쿠폰, 포인트 순서로 부분 취소한다")
    void partiallyCancelsCouponAndPointPayment() {
        Scenario scenario = prepareScenario(
                amount("60000"),
                amount("30000"),
                amount("15000"),
                amount("15000"),
                amount("35000"),
                6
        );

        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 1L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 2L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 3L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 4L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 5L, 1L));
        cancelService.partialCancel(new User(1L), new PartialCancelAction("order-key", 6L, 1L));

        assertCancelRecords(
                List.of(
                        expected("10000", "0", "0"),
                        expected("10000", "0", "0"),
                        expected("0", "10000", "0"),
                        expected("5000", "5000", "0"),
                        expected("5000", "0", "5000"),
                        expected("0", "0", "10000")
                )
        );
        assertTransactionHistories(
                List.of(
                        expected("10000", "0", "0"),
                        expected("10000", "0", "0"),
                        expected("0", "10000", "0"),
                        expected("5000", "5000", "0"),
                        expected("5000", "0", "5000"),
                        expected("0", "0", "10000")
                )
        );
        assertBalance(scenario.balance(), "0", "0", "0", "30000", "15000", "15000");
        assertOrderItemsCanceled(scenario.orderItemMap());
    }

    private Scenario prepareScenario(
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
        OrderEntity order = entityWithId(new OrderEntity(1L, "order-key", "주문", orderAmount, OrderState.PAID), orderId);
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
                        .externalPaymentKey("payment-key")
                        .paidAt(LocalDateTime.now())
                        .build(),
                paymentId
        );
        CancelBalanceEntity balance = entityWithId(
                new CancelBalanceEntity(orderId, paymentId, paidAmount, usedPoint, couponDiscount),
                50L
        );
        Map<Long, OrderItemEntity> orderItemMap = createOrderItems(orderId, itemCount);
        AtomicLong cancelId = new AtomicLong(100L);

        given(orderRepository.findByOrderKeyAndStateAndStatus("order-key", OrderState.PAID, EntityStatus.ACTIVE))
                .willReturn(Optional.of(order));
        given(orderRepository.findByOrderKeyAndStatus("order-key", EntityStatus.ACTIVE))
                .willReturn(Optional.of(order));
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));
        given(cancelBalanceRepository.findByOrderId(orderId)).willReturn(Optional.of(balance));
        given(orderItemRepository.findById(anyLong()))
                .willAnswer(invocation -> Optional.ofNullable(orderItemMap.get(invocation.getArgument(0))));
        given(orderItemRepository.findByOrderId(orderId)).willReturn(List.copyOf(orderItemMap.values()));
        given(cancelRepository.save(any(CancelEntity.class)))
                .willAnswer(invocation -> entityWithId(invocation.getArgument(0), cancelId.getAndIncrement()));

        if (ownedCouponId != null) {
            OwnedCouponEntity ownedCoupon = entityWithId(
                    OwnedCouponEntity.builder()
                            .userId(1L)
                            .couponId(40L)
                            .state(OwnedCouponState.USED)
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

        return new Scenario(balance, orderItemMap);
    }

    private void assertCancelRecords(List<ExpectedCancel> expectedCancels) {
        ArgumentCaptor<CancelEntity> captor = ArgumentCaptor.forClass(CancelEntity.class);
        verify(cancelRepository, org.mockito.Mockito.times(expectedCancels.size())).save(captor.capture());

        List<CancelEntity> cancels = captor.getAllValues();
        for (int index = 0; index < expectedCancels.size(); index++) {
            ExpectedCancel expected = expectedCancels.get(index);
            CancelEntity actual = cancels.get(index);

            assertThat(actual.getCanceledPaidAmount()).isEqualByComparingTo(expected.paidAmount());
            assertThat(actual.getCanceledCouponAmount()).isEqualByComparingTo(expected.couponAmount());
            assertThat(actual.getCanceledPointAmount()).isEqualByComparingTo(expected.pointAmount());
        }
    }

    private void assertTransactionHistories(List<ExpectedCancel> expectedHistories) {
        ArgumentCaptor<TransactionHistoryEntity> captor = ArgumentCaptor.forClass(TransactionHistoryEntity.class);
        verify(transactionHistoryRepository, org.mockito.Mockito.times(expectedHistories.size())).save(captor.capture());

        List<TransactionHistoryEntity> histories = captor.getAllValues();
        for (int index = 0; index < expectedHistories.size(); index++) {
            ExpectedCancel expected = expectedHistories.get(index);
            TransactionHistoryEntity actual = histories.get(index);

            assertThat(actual.getType()).isEqualTo(TransactionType.PARTIAL_CANCELED);
            assertThat(actual.getPaidAmount()).isEqualByComparingTo(expected.paidAmount());
            assertThat(actual.getCanceledCouponAmount()).isEqualByComparingTo(expected.couponAmount());
            assertThat(actual.getCanceledPointAmount()).isEqualByComparingTo(expected.pointAmount());
        }
    }

    private void assertBalance(
            CancelBalanceEntity balance,
            String cancelablePaidAmount,
            String cancelableCouponAmount,
            String cancelablePointAmount,
            String canceledPaidAmount,
            String canceledCouponAmount,
            String canceledPointAmount
    ) {
        assertThat(balance.getCancellablePaidAmount()).isEqualByComparingTo(cancelablePaidAmount);
        assertThat(balance.getCancellableCouponAmount()).isEqualByComparingTo(cancelableCouponAmount);
        assertThat(balance.getCancellablePointAmount()).isEqualByComparingTo(cancelablePointAmount);
        assertThat(balance.getCancelledPaidAmount()).isEqualByComparingTo(canceledPaidAmount);
        assertThat(balance.getCancelledCouponAmount()).isEqualByComparingTo(canceledCouponAmount);
        assertThat(balance.getCancelledPointAmount()).isEqualByComparingTo(canceledPointAmount);
    }

    private void assertOrderItemsCanceled(Map<Long, OrderItemEntity> orderItemMap) {
        assertThat(orderItemMap.values())
                .allSatisfy(item -> {
                    assertThat(item.getCanceledQuantity()).isEqualTo(1L);
                    assertThat(item.getState()).isEqualTo(OrderState.CANCELED);
                });
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

    private ExpectedCancel expected(String paidAmount, String couponAmount, String pointAmount) {
        return new ExpectedCancel(amount(paidAmount), amount(couponAmount), amount(pointAmount));
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

    private record Scenario(CancelBalanceEntity balance, Map<Long, OrderItemEntity> orderItemMap) {}

    private record ExpectedCancel(BigDecimal paidAmount, BigDecimal couponAmount, BigDecimal pointAmount) {}
}
