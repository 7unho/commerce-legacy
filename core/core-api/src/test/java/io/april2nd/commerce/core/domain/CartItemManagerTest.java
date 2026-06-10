package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.storage.db.core.BaseEntity;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartItemManagerTest {
    @InjectMocks private CartItemManager cartItemManager;
    @Mock private CartItemRepository cartItemRepository;

    @Test
    void savesNewCartItemWithOwnerInformation() {
        CartOwner owner = new CartOwner(10L, 1L);
        AddCartItem item = new AddCartItem(10L, 100L, 1000L, 2L);
        given(cartItemRepository.findByCartIdAndProductIdAndProductOptionId(10L, 100L, 1000L))
                .willReturn(null);
        given(cartItemRepository.save(any())).willAnswer(invocation -> entityWithId(invocation.getArgument(0), 20L));

        Long result = cartItemManager.addItem(owner, item);

        assertThat(result).isEqualTo(20L);
        verify(cartItemRepository).save(any(CartItemEntity.class));
    }

    @Test
    void reactivatesDeletedItemAndAppliesRequestedQuantity() {
        CartOwner owner = new CartOwner(10L, 1L);
        AddCartItem item = new AddCartItem(10L, 100L, 1000L, 2L);
        CartItemEntity existing = entityWithId(new CartItemEntity(1L, 10L, 100L, 1000L, 5L), 20L);
        existing.delete();
        given(cartItemRepository.findByCartIdAndProductIdAndProductOptionId(10L, 100L, 1000L))
                .willReturn(existing);

        cartItemManager.addItem(owner, item);

        assertThat(existing.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(existing.getQuantity()).isEqualTo(2L);
    }

    @Test
    void modifiesOnlyItemsOwnedByUser() {
        CartItemEntity existing = entityWithId(new CartItemEntity(1L, 10L, 100L, 1000L, 2L), 20L);
        given(cartItemRepository.findByUserIdAndIdAndStatus(1L, 20L, EntityStatus.ACTIVE))
                .willReturn(Optional.of(existing));

        cartItemManager.modifyItem(1L, new ModifyCartItem(20L, 5L));

        assertThat(existing.getQuantity()).isEqualTo(5L);
    }

    @Test
    void rejectsModificationByNonOwner() {
        given(cartItemRepository.findByUserIdAndIdAndStatus(2L, 20L, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemManager.modifyItem(2L, new ModifyCartItem(20L, 5L)))
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
