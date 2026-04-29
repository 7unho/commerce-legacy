package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import io.april2nd.commerce.storage.db.core.ProductEntity;
import io.april2nd.commerce.storage.db.core.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartReader {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public Cart getCart(User user) {
        List<CartItemEntity> items = cartItemRepository.findByUserIdAndStatus(user.id(), EntityStatus.ACTIVE);
        Map<Long, ProductEntity> productMap = productRepository.findAllById(
                        items.stream()
                                .map(CartItemEntity::getProductId)
                                .collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(
                        ProductEntity::getId,
                        it -> it
                ));

        return new Cart(
                user.id(),
                items.stream()
                        .filter(it -> productMap.containsKey(it.getProductId()))
                        .map(it ->
                                new CartItem(
                                        it.getId(),
                                        new Product(
                                                productMap.get(it.getProductId()).getId(),
                                                productMap.get(it.getProductId()).getName(),
                                                productMap.get(it.getProductId()).getThumbnailUrl(),
                                                productMap.get(it.getProductId()).getDescription(),
                                                productMap.get(it.getProductId()).getShortDescription(),
                                                new Price(
                                                        productMap.get(it.getProductId()).getCostPrice(),
                                                        productMap.get(it.getProductId()).getSalesPrice(),
                                                        productMap.get(it.getProductId()).getDiscountedPrice()
                                                )
                                        ),
                                        it.getQuantity()
                                )
                        )
                        .collect(Collectors.toList())
        );
    }
}
