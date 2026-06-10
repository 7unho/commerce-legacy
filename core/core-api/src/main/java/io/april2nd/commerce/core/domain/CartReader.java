package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartReader {
    private final CartItemRepository cartItemRepository;
    private final ProductFinder productFinder;

    public Cart getCart(User user) {
        List<CartItemEntity> items = cartItemRepository.findByUserIdAndStatus(user.id(), EntityStatus.ACTIVE);
        Map<Long, Product> productMap = productFinder.findAll(
                        items.stream()
                                .map(CartItemEntity::getProductId)
                                .collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(
                        Product::id,
                        it -> it
                ));

        return new Cart(
                user.id(),
                items.stream()
                        .filter(it -> productMap.containsKey(it.getProductId()))
                        .map(it ->
                                new CartItem(
                                        it.getId(),
                                        productMap.get(it.getProductId()),
                                        it.getQuantity()
                                )
                        )
                        .collect(Collectors.toList())
        );
    }
}
