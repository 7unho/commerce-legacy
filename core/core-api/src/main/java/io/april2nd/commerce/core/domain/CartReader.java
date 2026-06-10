package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartReader {
    private final CartRepository cartRepository;
    private final CartAccessRepository cartAccessRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductFinder productFinder;
    private final ProductOptionFinder productOptionFinder;

    public Cart getCart(Long userId) {
        List<CartEntity> carts = cartRepository.findByUserIdAndTypeAndStatus(userId, CartType.DEFAULT, EntityStatus.ACTIVE);
        if (carts.isEmpty()) throw new CoreException(ErrorType.NOT_FOUND_DATA);
        if (carts.size() > 1) throw new CoreException(ErrorType.DEFAULT_ERROR);

        return read(userId, carts.get(0).getId());
    }

    public Cart getSharedCart(Long userId, Long cartId) {
        CartEntity cart = cartRepository.findByIdAndTypeAndStatus(cartId, CartType.SHARED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        CartAccessEntity access = cartAccessRepository.findByCartIdAndAccessUserIdAndStatus(cart.getId(), userId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.CART_SHARED_NOT_FOUND));

        if (access.isExpired()) throw new CoreException(ErrorType.CART_SHARED_EXPIRED);

        return read(cart.getUserId(), cart.getId());
    }

    public List<CartAccess> getCartAccessList(Long userId) {
        return cartAccessRepository.findByAccessUserIdAndStatus(userId, EntityStatus.ACTIVE)
                .stream()
                .filter(CartAccessEntity::isNotExpired)
                .map(it ->
                        new CartAccess(
                                it.getAccessKey(),
                                it.getCartId(),
                                it.getType(),
                                it.getUserId(),
                                it.getExpiredAt(),
                                it.getCreatedAt(),
                                it.getUpdatedAt()
                        )
                )
                .collect(Collectors.toList());
    }

    private Cart read(Long userId, Long cartId) {
        List<CartItemEntity> items = cartItemRepository.findByCartIdAndStatus(cartId, EntityStatus.ACTIVE);
        if (items.isEmpty()) return new Cart(userId, Collections.emptyList());

        List<Long> productIds = items.stream().map(CartItemEntity::getProductId).collect(Collectors.toList());
        List<Long> productOptionsIds = items.stream().map(CartItemEntity::getProductOptionId).collect(Collectors.toList());
        List<Product> products = productFinder.find(productIds);
        List<ProductOption> productOptions = productOptionFinder.find(productOptionsIds, EntityStatus.ACTIVE);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::id, Function.identity()));
        Map<Long, ProductOption> productOptionMap = productOptions.stream().collect(Collectors.toMap(ProductOption::id, Function.identity()));

        List<CartItem> mappedItems = items.stream()
                .filter(it -> productMap.containsKey(it.getProductId()) && productOptionMap.containsKey(it.getProductOptionId()))
                .map(it ->
                        new CartItem(
                                it.getId(),
                                productMap.get(it.getProductId()),
                                productOptionMap.get(it.getProductOptionId()),
                                it.getQuantity()
                        )
                )
                .collect(Collectors.toList());

        return new Cart(userId, mappedItems);
    }
}
