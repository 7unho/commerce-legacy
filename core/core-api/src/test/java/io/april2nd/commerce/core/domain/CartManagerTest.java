package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartManagerTest {

    @InjectMocks
    private CartManager cartManager;

    @Mock
    private CartItemRepository cartItemRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L);
    }

    @Test
    @DisplayName("장바구니에 상품이 없는 경우 신규 저장한다")
    void add_new_item() {
        // given
        AddCartItem item = new AddCartItem(100L, 2L);
        given(cartItemRepository.findByUserIdAndProductId(user.id(), item.productId())).willReturn(null);
        CartItemEntity savedEntity = new CartItemEntity(user.id(), item.productId(), item.quantity());
        given(cartItemRepository.save(any())).willReturn(savedEntity);

        // when
        Long resultId = cartManager.add(user, item);

        // then
        verify(cartItemRepository).save(any());
    }

    @Test
    @DisplayName("장바구니에 이미 상품이 있는 경우 수량을 합산한다")
    void add_existing_item() {
        // given
        AddCartItem item = new AddCartItem(100L, 2L);
        CartItemEntity existing = new CartItemEntity(user.id(), item.productId(), 3L);
        given(cartItemRepository.findByUserIdAndProductId(user.id(), item.productId())).willReturn(existing);

        // when
        cartManager.add(user, item);

        // then
        assertThat(existing.getQuantity()).isEqualTo(5L);
    }

    @Test
    @DisplayName("삭제된 장바구니 상품을 다시 추가하는 경우 활성화하고 수량을 새로 설정한다")
    void add_deleted_item() {
        // given
        AddCartItem item = new AddCartItem(100L, 2L);
        CartItemEntity existing = new CartItemEntity(user.id(), item.productId(), 3L);
        existing.delete();
        given(cartItemRepository.findByUserIdAndProductId(user.id(), item.productId())).willReturn(existing);

        // when
        cartManager.add(user, item);

        // then
        assertThat(existing.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(existing.getQuantity()).isEqualTo(2L);
    }

    @Test
    @DisplayName("장바구니 아이템 수량을 수정한다")
    void modify_item() {
        // given
        ModifyCartItem item = new ModifyCartItem(1L, 5L);
        CartItemEntity existing = new CartItemEntity(user.id(), 100L, 3L);
        given(cartItemRepository.findByUserIdAndIdAndStatus(user.id(), item.cartItemId(), EntityStatus.ACTIVE))
                .willReturn(Optional.of(existing));

        // when
        cartManager.modify(user, item);

        // then
        assertThat(existing.getQuantity()).isEqualTo(5L);
    }

    @Test
    @DisplayName("장바구니 아이템을 삭제(Soft Delete)한다")
    void delete_item() {
        // given
        Long cartItemId = 1L;
        CartItemEntity existing = new CartItemEntity(user.id(), 100L, 3L);
        given(cartItemRepository.findByUserIdAndIdAndStatus(user.id(), cartItemId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(existing));

        // when
        cartManager.delete(user, cartItemId);

        // then
        assertThat(existing.getStatus()).isEqualTo(EntityStatus.DELETED);
    }
}
