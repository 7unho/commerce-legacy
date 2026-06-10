package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.storage.db.core.BaseEntity;
import io.april2nd.commerce.storage.db.core.CartAccessEntity;
import io.april2nd.commerce.storage.db.core.CartAccessRepository;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
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
class CartHandlerTest {
    @InjectMocks private CartHandler cartHandler;
    @Mock private CartRepository cartRepository;
    @Mock private CartAccessRepository cartAccessRepository;
    @Mock private CartItemRepository cartItemRepository;

    @Test
    void createsSharedCartAndOwnerAccess() {
        given(cartRepository.save(any())).willAnswer(invocation -> entityWithId(invocation.getArgument(0), 10L));
        given(cartAccessRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        CartAccess result = cartHandler.createSharedCart(1L);

        assertThat(result.cartId()).isEqualTo(10L);
        assertThat(result.type()).isEqualTo(CartType.SHARED);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.accessKey()).isNotBlank();
        assertThat(result.expiredAt()).isAfter(LocalDateTime.now().plusDays(6));
    }

    @Test
    void grantsSharedCartAccess() {
        CartAccessEntity ownerAccess = ownerAccess(LocalDateTime.now().plusDays(1));
        given(cartAccessRepository.findByAccessKeyAndStatus("owner-key", EntityStatus.ACTIVE))
                .willReturn(Optional.of(ownerAccess));
        given(cartAccessRepository.findByCartIdAndAccessUserId(10L, 2L)).willReturn(Optional.empty());

        cartHandler.access(2L, "owner-key");

        verify(cartAccessRepository).save(any(CartAccessEntity.class));
    }

    @Test
    void duplicateAccessIsIdempotent() {
        CartAccessEntity ownerAccess = ownerAccess(LocalDateTime.now().plusDays(1));
        CartAccessEntity memberAccess = new CartAccessEntity(
                "member-key", 10L, CartType.SHARED, 1L, 2L, LocalDateTime.now().plusDays(1)
        );
        given(cartAccessRepository.findByAccessKeyAndStatus("owner-key", EntityStatus.ACTIVE))
                .willReturn(Optional.of(ownerAccess));
        given(cartAccessRepository.findByCartIdAndAccessUserId(10L, 2L)).willReturn(Optional.of(memberAccess));

        cartHandler.access(2L, "owner-key");

        verify(cartAccessRepository, never()).save(any());
    }

    @Test
    void reactivatesDeletedMemberAccess() {
        CartAccessEntity ownerAccess = ownerAccess(LocalDateTime.now().plusDays(1));
        CartAccessEntity memberAccess = new CartAccessEntity(
                "member-key", 10L, CartType.SHARED, 1L, 2L, LocalDateTime.now().plusDays(1)
        );
        memberAccess.delete();
        given(cartAccessRepository.findByAccessKeyAndStatus("owner-key", EntityStatus.ACTIVE))
                .willReturn(Optional.of(ownerAccess));
        given(cartAccessRepository.findByCartIdAndAccessUserId(10L, 2L)).willReturn(Optional.of(memberAccess));

        cartHandler.access(2L, "owner-key");

        assertThat(memberAccess.isActive()).isTrue();
        verify(cartAccessRepository, never()).save(any());
    }

    @Test
    void rejectsExpiredAccessKey() {
        given(cartAccessRepository.findByAccessKeyAndStatus("owner-key", EntityStatus.ACTIVE))
                .willReturn(Optional.of(ownerAccess(LocalDateTime.now().minusSeconds(1))));

        assertThatThrownBy(() -> cartHandler.access(2L, "owner-key"))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void removesSharedCartAndRelatedData() {
        CartEntity cart = entityWithId(new CartEntity(CartType.SHARED, 1L), 10L);
        CartAccessEntity access = ownerAccess(LocalDateTime.now().plusDays(1));
        CartItemEntity item = new CartItemEntity(1L, 10L, 100L, 1000L, 1L);
        given(cartRepository.findByIdAndUserIdAndStatus(10L, 1L, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));
        given(cartAccessRepository.findByCartIdAndStatus(10L, EntityStatus.ACTIVE)).willReturn(List.of(access));
        given(cartItemRepository.findByCartIdAndStatus(10L, EntityStatus.ACTIVE)).willReturn(List.of(item));

        cartHandler.remove(1L, 10L);

        assertThat(cart.isDeleted()).isTrue();
        assertThat(access.isDeleted()).isTrue();
        assertThat(item.isDeleted()).isTrue();
    }

    @Test
    void rejectsDeletedAccessKey() {
        given(cartAccessRepository.findByAccessKeyAndStatus("deleted-key", EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> cartHandler.access(2L, "deleted-key"))
                .isInstanceOf(CoreException.class);
    }

    private static CartAccessEntity ownerAccess(LocalDateTime expiredAt) {
        return new CartAccessEntity("owner-key", 10L, CartType.SHARED, 1L, 1L, expiredAt);
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
