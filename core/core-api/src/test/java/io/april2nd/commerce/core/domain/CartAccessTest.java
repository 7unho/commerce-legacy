package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CartAccessTest {
    @Test
    void containsSharedCartAccessData() {
        LocalDateTime now = LocalDateTime.now();
        CartAccess access = new CartAccess(
                "access-key", 1L, CartType.SHARED, 2L, now.plusDays(7), now, now
        );

        assertThat(access.accessKey()).isEqualTo("access-key");
        assertThat(access.cartId()).isEqualTo(1L);
        assertThat(access.type()).isEqualTo(CartType.SHARED);
        assertThat(access.userId()).isEqualTo(2L);
        assertThat(access.expiredAt()).isEqualTo(now.plusDays(7));
    }
}
