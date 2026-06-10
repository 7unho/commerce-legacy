package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.storage.db.core.BaseEntity;
import io.april2nd.commerce.storage.db.core.CartAccessEntity;
import io.april2nd.commerce.storage.db.core.CartAccessRepository;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CartVerifierTest {
    @InjectMocks private CartVerifier cartVerifier;
    @Mock private CartRepository cartRepository;
    @Mock private CartAccessRepository cartAccessRepository;

    @Test
    void verifiesDefaultCartOwner() {
        CartEntity cart = entityWithId(new CartEntity(CartType.DEFAULT, 1L), 10L);
        given(cartRepository.findByIdAndStatus(10L, EntityStatus.ACTIVE)).willReturn(Optional.of(cart));

        CartOwner result = cartVerifier.verifyAccess(1L, 10L);

        assertThat(result).isEqualTo(new CartOwner(10L, 1L));
    }

    @Test
    void verifiesAcceptedSharedCartMember() {
        CartEntity cart = entityWithId(new CartEntity(CartType.SHARED, 1L), 10L);
        CartAccessEntity access = new CartAccessEntity(
                "member-key", 10L, CartType.SHARED, 1L, 2L, LocalDateTime.now().plusDays(1)
        );
        given(cartRepository.findByIdAndStatus(10L, EntityStatus.ACTIVE)).willReturn(Optional.of(cart));
        given(cartAccessRepository.findByCartIdAndAccessUserIdAndStatus(10L, 2L, EntityStatus.ACTIVE))
                .willReturn(Optional.of(access));

        CartOwner result = cartVerifier.verifyAccess(2L, 10L);

        assertThat(result).isEqualTo(new CartOwner(10L, 1L));
    }

    @Test
    void rejectsExpiredSharedCartAccess() {
        CartEntity cart = entityWithId(new CartEntity(CartType.SHARED, 1L), 10L);
        CartAccessEntity access = new CartAccessEntity(
                "member-key", 10L, CartType.SHARED, 1L, 2L, LocalDateTime.now().minusSeconds(1)
        );
        given(cartRepository.findByIdAndStatus(10L, EntityStatus.ACTIVE)).willReturn(Optional.of(cart));
        given(cartAccessRepository.findByCartIdAndAccessUserIdAndStatus(10L, 2L, EntityStatus.ACTIVE))
                .willReturn(Optional.of(access));

        assertThatThrownBy(() -> cartVerifier.verifyAccess(2L, 10L))
                .isInstanceOf(CoreException.class);
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
