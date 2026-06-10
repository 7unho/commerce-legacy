package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CartReaderTest {
    @InjectMocks private CartReader cartReader;
    @Mock private CartRepository cartRepository;
    @Mock private CartAccessRepository cartAccessRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductFinder productFinder;
    @Mock private ProductOptionFinder productOptionFinder;

    @Test
    void readsDefaultCartWithProductOption() {
        CartEntity cart = entityWithId(new CartEntity(CartType.DEFAULT, 1L), 10L);
        given(cartRepository.findByUserIdAndTypeAndStatus(1L, CartType.DEFAULT, EntityStatus.ACTIVE))
                .willReturn(List.of(cart));
        given(cartItemRepository.findByCartIdAndStatus(10L, EntityStatus.ACTIVE))
                .willReturn(List.of(new CartItemEntity(1L, 10L, 100L, 1000L, 2L)));
        given(productFinder.find(anyList())).willReturn(List.of(product(100L)));
        given(productOptionFinder.find(anyList(), eq(EntityStatus.ACTIVE))).willReturn(List.of(option(1000L)));

        Cart result = cartReader.getCart(1L);

        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.product().id()).isEqualTo(100L);
            assertThat(item.productOption().id()).isEqualTo(1000L);
            assertThat(item.quantity()).isEqualTo(2L);
        });
    }

    @Test
    void sharedMemberReadsOwnersCartItems() {
        CartEntity cart = entityWithId(new CartEntity(CartType.SHARED, 1L), 10L);
        CartAccessEntity access = new CartAccessEntity(
                "member-key", 10L, CartType.SHARED, 1L, 2L, LocalDateTime.now().plusDays(1)
        );
        given(cartRepository.findByIdAndTypeAndStatus(10L, CartType.SHARED, EntityStatus.ACTIVE))
                .willReturn(Optional.of(cart));
        given(cartAccessRepository.findByCartIdAndAccessUserIdAndStatus(10L, 2L, EntityStatus.ACTIVE))
                .willReturn(Optional.of(access));
        given(cartItemRepository.findByCartIdAndStatus(10L, EntityStatus.ACTIVE))
                .willReturn(List.of(new CartItemEntity(1L, 10L, 100L, 1000L, 2L)));
        given(productFinder.find(anyList())).willReturn(List.of(product(100L)));
        given(productOptionFinder.find(anyList(), eq(EntityStatus.ACTIVE))).willReturn(List.of(option(1000L)));

        Cart result = cartReader.getSharedCart(2L, 10L);

        assertThat(result.items()).hasSize(1);
    }

    @Test
    void returnsOwnedAndAcceptedSharedCartAccesses() {
        CartAccessEntity ownerAccess = new CartAccessEntity(
                "owner-key", 10L, CartType.SHARED, 1L, 1L, LocalDateTime.now().plusDays(1)
        );
        CartAccessEntity memberAccess = new CartAccessEntity(
                "member-key", 20L, CartType.SHARED, 2L, 1L, LocalDateTime.now().plusDays(1)
        );
        given(cartAccessRepository.findByAccessUserIdAndStatus(1L, EntityStatus.ACTIVE))
                .willReturn(List.of(ownerAccess, memberAccess));

        List<CartAccess> result = cartReader.getCartAccessList(1L);

        assertThat(result).extracting(CartAccess::cartId).containsExactly(10L, 20L);
    }

    private static Product product(Long id) {
        return new Product(
                id, "상품", "url", "설명", "짧은 설명",
                new Price(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN), LocalDateTime.now()
        );
    }

    private static ProductOption option(Long id) {
        return new ProductOption(
                id, 100L, "옵션", "옵션 설명",
                new Price(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)
        );
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
