package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.BaseEntity;
import io.april2nd.commerce.storage.db.core.OrderEntity;
import io.april2nd.commerce.storage.db.core.OrderItemEntity;
import io.april2nd.commerce.storage.db.core.OrderItemRepository;
import io.april2nd.commerce.storage.db.core.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderManagerTest {
    private final OrderKeyGenerator orderKeyGenerator;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderManager orderManager;

    OrderManagerTest(
            @Mock OrderKeyGenerator orderKeyGenerator,
            @Mock OrderRepository orderRepository,
            @Mock OrderItemRepository orderItemRepository
    ) {
        this.orderKeyGenerator = orderKeyGenerator;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderManager = new OrderManager(orderKeyGenerator, orderRepository, orderItemRepository);
    }

    @Test
    void createsOrderUsingProductOptionAsOrderUnit() {
        Product product = new Product(
                100L,
                "상품",
                "thumbnail",
                "description",
                "short description",
                price("10000"),
                LocalDateTime.MIN
        );
        ProductOption productOption = new ProductOption(
                1000L,
                product.id(),
                "빨강 / L",
                "옵션 설명",
                price("12000")
        );
        NewOrder newOrder = new NewOrder(
                1L,
                List.of(new NewOrderItem(product.id(), productOption.id(), 2L))
        );
        given(orderKeyGenerator.generate()).willReturn("order-key");
        given(orderRepository.save(any(OrderEntity.class)))
                .willAnswer(invocation -> entityWithId(invocation.getArgument(0), 10L));

        String orderKey = orderManager.create(1L, newOrder, List.of(product), List.of(productOption));

        assertThat(orderKey).isEqualTo("order-key");

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getTotalPrice()).isEqualByComparingTo("24000");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItemEntity>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        assertThat(itemsCaptor.getValue()).singleElement().satisfies(item -> {
            assertThat(item.getProductId()).isEqualTo(product.id());
            assertThat(item.getProductOptionId()).isEqualTo(productOption.id());
            assertThat(item.getProductName()).isEqualTo(product.name());
            assertThat(item.getProductOptionName()).isEqualTo(productOption.name());
            assertThat(item.getUnitPrice()).isEqualByComparingTo("12000");
            assertThat(item.getTotalPrice()).isEqualByComparingTo("24000");
        });
    }

    private static Price price(String discountedPrice) {
        BigDecimal price = new BigDecimal(discountedPrice);
        return new Price(price, price, price);
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
}
