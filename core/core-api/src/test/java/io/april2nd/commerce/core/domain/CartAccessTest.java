package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.support.error.CoreException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartAccessTest {

    @Test
    void keepsCartTypeAndAccessData() {
        LocalDateTime now = LocalDateTime.now();
        CartAccess access = new CartAccess(
                "access-key", 1L, CartType.SHARED, 2L, now.plusDays(1), now, now
        );

        assertThat(access.type()).isEqualTo(CartType.SHARED);
        assertThat(access.accessKey()).isEqualTo("access-key");
        assertThat(access.cartId()).isEqualTo(1L);
        assertThat(access.userId()).isEqualTo(2L);
    }

    @Test
    void rejectsAnotherUser() {
        LocalDateTime now = LocalDateTime.now();
        CartAccess access = new CartAccess(
                "access-key", 1L, CartType.SHARED, 2L, now.plusDays(1), now, now
        );

        assertThatThrownBy(() -> access.validate(new User(3L), now))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void rejectsExpiredAccess() {
        LocalDateTime now = LocalDateTime.now();
        CartAccess access = new CartAccess(
                "access-key", 1L, CartType.SHARED, 2L, now, now.minusDays(7), now
        );

        assertThatThrownBy(() -> access.validate(new User(2L), now))
                .isInstanceOf(CoreException.class);
    }
}
