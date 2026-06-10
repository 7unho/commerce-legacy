package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.AddCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.request.CreateSharedCartRequest;
import io.april2nd.commerce.core.api.controller.v1.request.ModifyCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.response.AcceptSharedCartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.CartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.CreateSharedCartResponse;
import io.april2nd.commerce.core.domain.*;
import io.april2nd.commerce.core.support.response.ApiResponse;
import io.april2nd.commerce.core.support.response.ResultType;
import org.junit.jupiter.api.DisplayName;
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

    @InjectMocks
    private CartController cartController;

    @Mock
    private CartService cartService;

    @Test
    @DisplayName("장바구니를 조회한다")
    void get_cart() {
        // given
        User user = new User(1L);
        Cart cart = new Cart(1L, List.of(
                new CartItem(1L, new Product(100L, "Product 1", "url1", "desc1", "short1",
                        new Price(BigDecimal.valueOf(1000), BigDecimal.valueOf(1500), BigDecimal.valueOf(1200)),
                        LocalDateTime.now()), 2L)
        ));
        given(cartService.getCart(user)).willReturn(cart);

        // when
        ApiResponse<CartResponse> response = cartController.getCart(user);

        // then
        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(response.getData().items()).hasSize(1);
        assertThat(response.getData().items().get(0).productId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("장바구니에 아이템을 추가한다")
    void add_cart_item() {
        // given
        User user = new User(1L);
        AddCartItemRequest request = new AddCartItemRequest(100L, 2L);

        // when
        ApiResponse<Void> response = cartController.addCartItem(user, request);

        // then
        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        verify(cartService).addCartItem(eq(user), any(AddCartItem.class));
    }

    @Test
    @DisplayName("장바구니 아이템 수량을 수정한다")
    void modify_cart_item() {
        // given
        User user = new User(1L);
        Long cartItemId = 1L;
        ModifyCartItemRequest request = new ModifyCartItemRequest(5L);

        // when
        ApiResponse<Void> response = cartController.modifyCartItem(user, cartItemId, request);

        // then
        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        verify(cartService).modifyCartItem(eq(user), any(ModifyCartItem.class));
    }

    @Test
    @DisplayName("장바구니 아이템을 삭제한다")
    void delete_cart_item() {
        // given
        User user = new User(1L);
        Long cartItemId = 1L;

        // when
        ApiResponse<Void> response = cartController.deleteCartItem(user, cartItemId);

        // then
        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        verify(cartService).deleteCartItem(user, cartItemId);
    }

    @Test
    @DisplayName("공유 장바구니를 생성한다")
    void create_shared_cart() {
        // given
        User user = new User(1L);
        CreateSharedCartRequest request = new CreateSharedCartRequest("공유 장바구니");
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);
        given(cartService.createSharedCart(eq(user), any(CreateSharedCart.class)))
                .willReturn(new CreatedSharedCart(1L, "access-key", expiredAt));

        // when
        ApiResponse<CreateSharedCartResponse> response = cartController.createSharedCart(user, request);

        // then
        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(response.getData().cartId()).isEqualTo(1L);
        verify(cartService).createSharedCart(eq(user), any(CreateSharedCart.class));
    }

    @Test
    @DisplayName("접근 키로 공유 장바구니를 수락한다")
    void accept_shared_cart() {
        // given
        User user = new User(2L);
        String accessKey = "access-key";
        given(cartService.acceptSharedCart(user, accessKey)).willReturn(1L);

        // when
        ApiResponse<AcceptSharedCartResponse> response = cartController.acceptSharedCart(user, accessKey);

        // then
        assertThat(response.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(response.getData().cartId()).isEqualTo(1L);
        verify(cartService).acceptSharedCart(user, accessKey);
    }
}
