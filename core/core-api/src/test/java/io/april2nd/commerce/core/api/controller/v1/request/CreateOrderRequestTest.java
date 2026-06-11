package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.NewOrder;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.error.CoreException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class CreateOrderRequestTest {
    @Test
    void convertsProductsAndOptionsToNewOrderItems() {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(
                        new CreateOrderRequest.OrderProductRequest(
                                100L,
                                List.of(
                                        new CreateOrderRequest.OrderOptionRequest(1000L, 1L),
                                        new CreateOrderRequest.OrderOptionRequest(1001L, 2L)
                                )
                        ),
                        new CreateOrderRequest.OrderProductRequest(
                                200L,
                                List.of(new CreateOrderRequest.OrderOptionRequest(2000L, 3L))
                        )
                )
        );

        NewOrder result = request.toNewOrder(new User(1L));

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.items()).hasSize(3);
        assertThat(result.items()).extracting("productId", "productOptionId", "quantity")
                .containsExactly(
                        tuple(100L, 1000L, 1L),
                        tuple(100L, 1001L, 2L),
                        tuple(200L, 2000L, 3L)
                );
    }

    @Test
    void rejectsEmptyProducts() {
        CreateOrderRequest request = new CreateOrderRequest(List.of());

        assertThatThrownBy(() -> request.toNewOrder(new User(1L)))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void rejectsProductWithoutOptions() {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new CreateOrderRequest.OrderProductRequest(100L, List.of()))
        );

        assertThatThrownBy(() -> request.toNewOrder(new User(1L)))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void rejectsNonPositiveQuantity() {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(
                        new CreateOrderRequest.OrderProductRequest(
                                100L,
                                List.of(new CreateOrderRequest.OrderOptionRequest(1000L, 0L))
                        )
                )
        );

        assertThatThrownBy(() -> request.toNewOrder(new User(1L)))
                .isInstanceOf(CoreException.class);
    }
}
