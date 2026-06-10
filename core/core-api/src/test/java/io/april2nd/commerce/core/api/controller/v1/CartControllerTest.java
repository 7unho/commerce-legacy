package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.AddCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.request.ModifyCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.response.CartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.SharedCartResponse;
import io.april2nd.commerce.core.domain.AddCartItem;
import io.april2nd.commerce.core.domain.Cart;
import io.april2nd.commerce.core.domain.CartAccess;
import io.april2nd.commerce.core.domain.CartItem;
import io.april2nd.commerce.core.domain.CartService;
import io.april2nd.commerce.core.domain.ModifyCartItem;
import io.april2nd.commerce.core.domain.Price;
import io.april2nd.commerce.core.domain.Product;
import io.april2nd.commerce.core.domain.ProductOption;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.support.response.ApiResponse;
import io.april2nd.commerce.core.support.response.ResultType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {
    @InjectMocks private CartController cartController;
    @Mock private CartService cartService;

    @Test
    void getsDefaultCart() {
        User user = new User(1L);
        given(cartService.getCart(user)).willReturn(new Cart(1L, List.of(cartItem())));

        ApiResponse<CartResponse> response = cartController.getCart(user);

        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(response.getData().items()).singleElement()
                .extracting(CartResponse.CartItemResponse::productId)
                .isEqualTo(100L);
    }

    @Test
    void addsCartItemWithCartAndOption() {
        User user = new User(1L);
        AddCartItemRequest request = new AddCartItemRequest(10L, 100L, 1000L, 2L);

        ApiResponse<Void> response = cartController.addCartItem(user, request);

        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        verify(cartService).addCartItem(eq(user), any(AddCartItem.class));
    }

    @Test
    void modifiesCartItem() {
        User user = new User(1L);

        ApiResponse<Void> response = cartController.modifyCartItem(
                user, 20L, new ModifyCartItemRequest(5L)
        );

        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        verify(cartService).modifyCartItem(eq(user), any(ModifyCartItem.class));
    }

    @Test
    void createsSharedCart() {
        User user = new User(1L);
        CartAccess access = access(10L, 1L);
        given(cartService.createSharedCarts(user)).willReturn(access);

        ApiResponse<SharedCartResponse> response = cartController.createSharedCart(user);

        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(response.getData().cartId()).isEqualTo(10L);
        assertThat(response.getData().accessKey()).isEqualTo("access-key");
    }

    @Test
    void getsAccessibleSharedCarts() {
        User user = new User(1L);
        given(cartService.getAccessibleCart(user)).willReturn(List.of(access(10L, 1L), access(20L, 2L)));

        ApiResponse<List<SharedCartResponse>> response = cartController.getSharedCarts(user);

        assertThat(response.getData()).extracting(SharedCartResponse::cartId).containsExactly(10L, 20L);
    }

    @Test
    void acceptsSharedCartByAccessKey() {
        User user = new User(2L);

        ApiResponse<Void> response = cartController.accessCart(user, "access-key");

        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        verify(cartService).access(user, "access-key");
    }

    private static CartAccess access(Long cartId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return new CartAccess(
                "access-key", cartId, CartType.SHARED, userId, now.plusDays(7), now, now
        );
    }

    private static CartItem cartItem() {
        Price price = new Price(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
        Product product = new Product(
                100L, "상품", "url", "설명", "짧은 설명", price, LocalDateTime.now()
        );
        ProductOption option = new ProductOption(1000L, 100L, "옵션", "옵션 설명", price);
        return new CartItem(20L, product, option, 2L);
    }
}
