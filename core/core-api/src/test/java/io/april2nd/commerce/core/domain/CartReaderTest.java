package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.BaseEntity;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CartReaderTest {
    @InjectMocks private CartReader cartReader;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductFinder productFinder;

    @Test
    void returnsEmptyCartWhenPersonalCartDoesNotExist() {
        User user = new User(1L);
        given(cartRepository.findByOwnerIdAndTypeAndStatus(user.id(), CartType.PERSONAL, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        Cart result = cartReader.getCart(user);

        assertThat(result.items()).isEmpty();
    }

    @Test
    void combinesActiveCartItemsWithProducts() {
        User user = new User(1L);
        CartEntity cart = entityWithId(new CartEntity(user.id(), CartType.PERSONAL, null, null, null), 10L);
        given(cartRepository.findByOwnerIdAndTypeAndStatus(user.id(), CartType.PERSONAL, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));
        given(cartItemRepository.findByCartIdAndStatus(cart.getId(), EntityStatus.ACTIVE))
                .willReturn(List.of(new CartItemEntity(cart.getId(), 100L, 2L)));
        given(productFinder.findAll(anyList())).willReturn(List.of(new Product(
                100L, "상품", "url", "desc", "short",
                new Price(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN), LocalDateTime.now()
        )));

        Cart result = cartReader.getCart(user);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).product().id()).isEqualTo(100L);
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
