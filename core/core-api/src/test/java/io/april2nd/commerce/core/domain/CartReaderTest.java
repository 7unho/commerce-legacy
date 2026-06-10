package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CartReaderTest {

    @InjectMocks
    private CartReader cartReader;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductFinder productFinder;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L);
    }

    @Test
    @DisplayName("사용자의 활성화된 장바구니 아이템과 상품 정보를 결합하여 반환한다")
    void get_cart_success() {
        // given
        CartItemEntity itemEntity1 = new CartItemEntity(user.id(), 100L, 2L);
        CartItemEntity itemEntity2 = new CartItemEntity(user.id(), 101L, 1L);
        given(cartItemRepository.findByUserIdAndStatus(user.id(), EntityStatus.ACTIVE))
                .willReturn(List.of(itemEntity1, itemEntity2));

        Product product1 = new Product(100L, "Product 1", "url1", "desc1", "short1",
                new Price(BigDecimal.valueOf(1000), BigDecimal.valueOf(1500), BigDecimal.valueOf(1200)),
                LocalDateTime.now());
        Product product2 = new Product(101L, "Product 2", "url2", "desc2", "short2",
                new Price(BigDecimal.valueOf(2000), BigDecimal.valueOf(2500), BigDecimal.valueOf(2200)),
                LocalDateTime.now());
        given(productFinder.findAll(anyList())).willReturn(List.of(product1, product2));

        // when
        Cart result = cartReader.getCart(user);

        // then
        assertThat(result.userId()).isEqualTo(user.id());
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).product().id()).isEqualTo(100L);
        assertThat(result.items().get(1).product().id()).isEqualTo(101L);
    }

    @Test
    @DisplayName("상품 정보가 없는 장바구니 아이템은 제외하고 반환한다")
    void get_cart_exclude_missing_product() {
        // given
        CartItemEntity itemEntity1 = new CartItemEntity(user.id(), 100L, 2L);
        given(cartItemRepository.findByUserIdAndStatus(user.id(), EntityStatus.ACTIVE))
                .willReturn(List.of(itemEntity1));

        given(productFinder.findAll(anyList())).willReturn(Collections.emptyList());

        // when
        Cart result = cartReader.getCart(user);

        // then
        assertThat(result.items()).isEmpty();
    }
}
