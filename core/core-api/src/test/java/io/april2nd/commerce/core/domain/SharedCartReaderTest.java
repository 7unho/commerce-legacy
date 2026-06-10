package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.SharedCartRole;
import io.april2nd.commerce.core.enums.SharedCartState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.storage.db.core.BaseEntity;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import io.april2nd.commerce.storage.db.core.CartMemberEntity;
import io.april2nd.commerce.storage.db.core.CartMemberRepository;
import io.april2nd.commerce.storage.db.core.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SharedCartReaderTest {
    @InjectMocks private SharedCartReader sharedCartReader;
    @Mock private CartRepository cartRepository;
    @Mock private CartMemberRepository cartMemberRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductFinder productFinder;

    @Test
    void returnsOwnedAndJoinedCartsWithRoleAndExpiration() {
        User user = new User(1L);
        CartEntity owned = entityWithMetadata(new CartEntity(
                user.id(), CartType.SHARED, "내 카트", "owned", LocalDateTime.now().plusDays(1)
        ), 10L, LocalDateTime.now().minusDays(1));
        CartEntity joined = entityWithMetadata(new CartEntity(
                2L, CartType.SHARED, "참여 카트", "joined", LocalDateTime.now().minusSeconds(1)
        ), 20L, LocalDateTime.now());
        given(cartRepository.findByOwnerIdAndTypeAndStatusOrderByCreatedAtDesc(
                user.id(), CartType.SHARED, EntityStatus.ACTIVE
        )).willReturn(List.of(owned));
        given(cartMemberRepository.findByUserIdAndStatus(user.id(), EntityStatus.ACTIVE))
                .willReturn(List.of(new CartMemberEntity(joined.getId(), user.id(), LocalDateTime.now())));
        given(cartRepository.findByIdInAndTypeAndStatus(List.of(joined.getId()), CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(List.of(joined));
        given(cartItemRepository.findByCartIdInAndStatus(List.of(owned.getId(), joined.getId()), EntityStatus.ACTIVE))
                .willReturn(List.of(
                        new CartItemEntity(owned.getId(), 100L, 1L),
                        new CartItemEntity(owned.getId(), 101L, 1L)
                ));

        List<SharedCartSummary> result = sharedCartReader.getSharedCarts(user);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).role()).isEqualTo(SharedCartRole.MEMBER);
        assertThat(result.get(0).state()).isEqualTo(SharedCartState.EXPIRED);
        assertThat(result.get(0).sharePath()).isNull();
        assertThat(result.get(1).role()).isEqualTo(SharedCartRole.OWNER);
        assertThat(result.get(1).itemCount()).isEqualTo(2L);
        assertThat(result.get(1).sharePath()).contains("owned");
    }

    @Test
    void memberCanReadActiveSharedCartItems() {
        User user = new User(2L);
        CartEntity cart = entityWithMetadata(new CartEntity(
                1L, CartType.SHARED, "공유", "token", LocalDateTime.now().plusDays(1)
        ), 10L, LocalDateTime.now());
        given(cartRepository.findByIdAndTypeAndStatus(cart.getId(), CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));
        given(cartMemberRepository.existsByCartIdAndUserIdAndStatus(cart.getId(), user.id(), EntityStatus.ACTIVE))
                .willReturn(true);
        given(cartItemRepository.findByCartIdAndStatus(cart.getId(), EntityStatus.ACTIVE))
                .willReturn(List.of(new CartItemEntity(cart.getId(), 100L, 2L)));
        given(productFinder.findAll(anyList())).willReturn(List.of(new Product(
                100L, "상품", "url", "desc", "short",
                new Price(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN), LocalDateTime.now()
        )));

        SharedCart result = sharedCartReader.getSharedCart(user, cart.getId());

        assertThat(result.role()).isEqualTo(SharedCartRole.MEMBER);
        assertThat(result.items()).hasSize(1);
    }

    @Test
    void rejectsUnknownUserFromSharedCartDetail() {
        User user = new User(3L);
        CartEntity cart = entityWithMetadata(new CartEntity(
                1L, CartType.SHARED, "공유", "token", LocalDateTime.now().plusDays(1)
        ), 10L, LocalDateTime.now());
        given(cartRepository.findByIdAndTypeAndStatus(cart.getId(), CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));

        assertThatThrownBy(() -> sharedCartReader.getSharedCart(user, cart.getId()))
                .isInstanceOf(CoreException.class);
    }

    private static CartEntity entityWithMetadata(CartEntity entity, Long id, LocalDateTime createdAt) {
        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, createdAt);
            return entity;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
