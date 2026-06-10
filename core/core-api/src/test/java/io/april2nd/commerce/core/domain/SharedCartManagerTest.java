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
class SharedCartManagerTest {
    @InjectMocks private SharedCartManager sharedCartManager;
    @Mock private CartRepository cartRepository;
    @Mock private CartMemberRepository cartMemberRepository;
    @Mock private CartItemRepository cartItemRepository;

    @Test
    void createsSharedCartWithSevenDayExpiration() {
        LocalDateTime before = LocalDateTime.now().plusDays(7);
        given(cartRepository.save(any())).willAnswer(invocation -> entityWithId(invocation.getArgument(0), 10L));

        CreatedSharedCart created = sharedCartManager.create(new User(1L), new CreateSharedCart("여행 준비"));

        assertThat(created.cartId()).isEqualTo(10L);
        assertThat(created.shareToken()).isNotBlank();
        assertThat(created.expiredAt()).isBetween(before.minusSeconds(1), LocalDateTime.now().plusDays(7).plusSeconds(1));
    }

    @Test
    void acceptsSharedCartIdempotently() {
        User user = new User(2L);
        CartEntity cart = entityWithId(new CartEntity(
                1L, CartType.SHARED, "공유", "token", LocalDateTime.now().plusDays(1)
        ), 10L);
        CartMemberEntity member = new CartMemberEntity(cart.getId(), user.id(), LocalDateTime.now());
        given(cartRepository.findByShareTokenAndTypeAndStatus("token", CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));
        given(cartMemberRepository.findByCartIdAndUserId(cart.getId(), user.id())).willReturn(Optional.of(member));

        Long result = sharedCartManager.accept(user, "token");

        assertThat(result).isEqualTo(cart.getId());
        verify(cartMemberRepository, never()).save(any());
    }

    @Test
    void rejectsAcceptWhenCartExpired() {
        CartEntity cart = entityWithId(new CartEntity(
                1L, CartType.SHARED, "공유", "token", LocalDateTime.now().minusSeconds(1)
        ), 10L);
        given(cartRepository.findByShareTokenAndTypeAndStatus("token", CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));

        assertThatThrownBy(() -> sharedCartManager.accept(new User(2L), "token"))
                .isInstanceOf(CoreException.class);
    }

    @Test
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

        sharedCartManager.delete(owner, cart.getId());

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
