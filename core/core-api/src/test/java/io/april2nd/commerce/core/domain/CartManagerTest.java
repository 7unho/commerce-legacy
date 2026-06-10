package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.storage.db.core.BaseEntity;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import io.april2nd.commerce.storage.db.core.CartMemberEntity;
import io.april2nd.commerce.storage.db.core.CartMemberRepository;
import io.april2nd.commerce.storage.db.core.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartManagerTest {
    @InjectMocks private CartManager cartManager;
    @Mock private CartRepository cartRepository;
    @Mock private CartMemberRepository cartMemberRepository;
    @Mock private CartItemRepository cartItemRepository;

    private User user;
    private CartEntity personalCart;

    @BeforeEach
    void setUp() {
        user = new User(1L);
        personalCart = entityWithId(new CartEntity(user.id(), CartType.PERSONAL, null, null, null), 10L);
    }

    @Test
    @DisplayName("개인 장바구니에 상품이 없는 경우 신규 저장한다")
    void addNewItem() {
        AddCartItem item = new AddCartItem(100L, 2L);
        given(cartRepository.findByOwnerIdAndTypeAndStatus(user.id(), CartType.PERSONAL, EntityStatus.ACTIVE))
                .willReturn(Optional.of(personalCart));
        given(cartItemRepository.findByCartIdAndProductId(personalCart.getId(), item.productId())).willReturn(null);
        given(cartItemRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        cartManager.add(user, item);

        verify(cartItemRepository).save(any(CartItemEntity.class));
    }

    @Test
    @DisplayName("이미 담긴 상품은 수량을 합산한다")
    void addExistingItem() {
        AddCartItem item = new AddCartItem(100L, 2L);
        CartItemEntity existing = new CartItemEntity(personalCart.getId(), item.productId(), 3L);
        given(cartRepository.findByOwnerIdAndTypeAndStatus(user.id(), CartType.PERSONAL, EntityStatus.ACTIVE))
                .willReturn(Optional.of(personalCart));
        given(cartItemRepository.findByCartIdAndProductId(personalCart.getId(), item.productId())).willReturn(existing);

        cartManager.add(user, item);

        assertThat(existing.getQuantity()).isEqualTo(5L);
    }

    @Test
    @DisplayName("삭제된 상품을 다시 담으면 활성화하고 요청 수량을 적용한다")
    void reactivateDeletedItem() {
        AddCartItem item = new AddCartItem(100L, 2L);
        CartItemEntity existing = new CartItemEntity(personalCart.getId(), item.productId(), 3L);
        existing.delete();
        given(cartRepository.findByOwnerIdAndTypeAndStatus(user.id(), CartType.PERSONAL, EntityStatus.ACTIVE))
                .willReturn(Optional.of(personalCart));
        given(cartItemRepository.findByCartIdAndProductId(personalCart.getId(), item.productId())).willReturn(existing);

        cartManager.add(user, item);

        assertThat(existing.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(existing.getQuantity()).isEqualTo(2L);
    }

    @Test
    @DisplayName("공유 장바구니 참여자는 상품을 추가할 수 있다")
    void memberCanAddSharedCartItem() {
        CartEntity sharedCart = entityWithId(new CartEntity(
                2L, CartType.SHARED, "공유", "token", LocalDateTime.now().plusDays(1)
        ), 20L);
        AddCartItem item = new AddCartItem(sharedCart.getId(), 100L, 2L);
        given(cartRepository.findByIdAndTypeAndStatus(sharedCart.getId(), CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(sharedCart));
        given(cartMemberRepository.findByCartIdAndUserId(sharedCart.getId(), user.id()))
                .willReturn(Optional.of(new CartMemberEntity(
                        sharedCart.getId(), user.id(), LocalDateTime.now()
                )));
        given(cartItemRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        cartManager.add(user, item);

        verify(cartItemRepository).save(any(CartItemEntity.class));
    }

    @Test
    @DisplayName("공유 장바구니 참여자는 상품 수량을 수정할 수 없다")
    void memberCannotModifySharedCartItem() {
        CartEntity sharedCart = entityWithId(new CartEntity(
                2L, CartType.SHARED, "공유", "token", LocalDateTime.now().plusDays(1)
        ), 20L);
        CartItemEntity item = entityWithId(new CartItemEntity(sharedCart.getId(), 100L, 2L), 30L);
        given(cartItemRepository.findByIdAndStatus(item.getId(), EntityStatus.ACTIVE)).willReturn(Optional.of(item));
        given(cartRepository.findById(sharedCart.getId())).willReturn(Optional.of(sharedCart));

        assertThatThrownBy(() -> cartManager.modify(user, new ModifyCartItem(item.getId(), 5L)))
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("공유 장바구니 소유자는 상품을 삭제할 수 있다")
    void ownerCanDeleteSharedCartItem() {
        CartEntity sharedCart = entityWithId(new CartEntity(
                user.id(), CartType.SHARED, "공유", "token", LocalDateTime.now().plusDays(1)
        ), 20L);
        CartItemEntity item = entityWithId(new CartItemEntity(sharedCart.getId(), 100L, 2L), 30L);
        given(cartItemRepository.findByIdAndStatus(item.getId(), EntityStatus.ACTIVE)).willReturn(Optional.of(item));
        given(cartRepository.findById(sharedCart.getId())).willReturn(Optional.of(sharedCart));

        cartManager.deleteItem(user, item.getId());

        assertThat(item.getStatus()).isEqualTo(EntityStatus.DELETED);
    }

    @Test
    @DisplayName("만료된 공유 장바구니에는 상품을 추가할 수 없다")
    void cannotAddExpiredSharedCartItem() {
        CartEntity sharedCart = entityWithId(new CartEntity(
                user.id(), CartType.SHARED, "공유", "token", LocalDateTime.now().minusSeconds(1)
        ), 20L);
        given(cartRepository.findByIdAndTypeAndStatus(sharedCart.getId(), CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(sharedCart));

        assertThatThrownBy(() -> cartManager.add(user, new AddCartItem(sharedCart.getId(), 100L, 1L)))
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("공유 장바구니는 정책에 따라 7일 뒤 만료된다")
    void createsSharedCartWithSevenDayExpiration() {
        LocalDateTime before = CartPolicy.SHARED_EXPIRATION_DAYS.expireAt(LocalDateTime.now());
        given(cartRepository.save(any())).willAnswer(invocation -> entityWithId(invocation.getArgument(0), 10L));

        CreatedSharedCart created = cartManager.createSharedCart(new User(1L), new CreateSharedCart("여행 준비"));

        assertThat(created.cartId()).isEqualTo(10L);
        assertThat(created.shareToken()).isNotBlank();
        assertThat(created.expiredAt()).isBetween(
                before.minusSeconds(1),
                CartPolicy.SHARED_EXPIRATION_DAYS.expireAt(LocalDateTime.now()).plusSeconds(1)
        );
    }

    @Test
    @DisplayName("이미 접근 권한이 있는 회원의 공유 장바구니 수락은 멱등하게 처리한다")
    void acceptsSharedCartIdempotently() {
        User memberUser = new User(2L);
        CartEntity cart = entityWithId(new CartEntity(
                1L, CartType.SHARED, "공유", "token", LocalDateTime.now().plusDays(1)
        ), 10L);
        CartMemberEntity member = new CartMemberEntity(cart.getId(), memberUser.id(), LocalDateTime.now());
        given(cartRepository.findByShareTokenAndTypeAndStatus("token", CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));
        given(cartMemberRepository.findByCartIdAndUserId(cart.getId(), memberUser.id()))
                .willReturn(Optional.of(member));

        Long result = cartManager.acceptSharedCart(memberUser, "token");

        assertThat(result).isEqualTo(cart.getId());
        verify(cartMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("만료된 공유 장바구니는 수락할 수 없다")
    void rejectsAcceptWhenCartExpired() {
        CartEntity cart = entityWithId(new CartEntity(
                1L, CartType.SHARED, "공유", "token", LocalDateTime.now().minusSeconds(1)
        ), 10L);
        given(cartRepository.findByShareTokenAndTypeAndStatus("token", CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartManager.acceptSharedCart(new User(2L), "token"))
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("소유자는 만료된 공유 장바구니와 연관 데이터를 삭제할 수 있다")
    void ownerCanDeleteExpiredCartAndRelatedData() {
        User owner = new User(1L);
        CartEntity cart = entityWithId(new CartEntity(
                owner.id(), CartType.SHARED, "공유", "token", LocalDateTime.now().minusDays(1)
        ), 10L);
        CartMemberEntity member = new CartMemberEntity(cart.getId(), 2L, LocalDateTime.now());
        CartItemEntity item = new CartItemEntity(cart.getId(), 100L, 1L);
        given(cartRepository.findByIdAndTypeAndStatus(cart.getId(), CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));
        given(cartMemberRepository.findByCartIdAndStatus(cart.getId(), EntityStatus.ACTIVE)).willReturn(List.of(member));
        given(cartItemRepository.findByCartIdAndStatus(cart.getId(), EntityStatus.ACTIVE)).willReturn(List.of(item));

        cartManager.deleteSharedCart(owner, cart.getId());

        assertThat(cart.getStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(member.getStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(item.getStatus()).isEqualTo(EntityStatus.DELETED);
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
