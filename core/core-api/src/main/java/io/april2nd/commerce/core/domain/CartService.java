package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import io.april2nd.commerce.storage.db.core.ProductEntity;
import io.april2nd.commerce.storage.db.core.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private CartItemRepository cartItemRepository;
    private ProductRepository productRepository;

    public Cart getCart(User user) {
        List<CartItemEntity> items = cartItemRepository.findByUserIdAndStatus(user.id(), EntityStatus.ACTIVE);
        Map<Long, ProductEntity> productMap = productRepository.findAllById(
                        items.stream()
                                .map(it -> it.getProductId())
                                .collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(
                        it -> it.getId(),
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

    @Transactional
    public Long addCartItem(User user, AddCartItem item) {
        CartItemEntity existing = cartItemRepository.findByUserIdAndProductId(user.id(), item.productId());

        if (existing == null) {
            CartItemEntity saved = cartItemRepository.save(
                    new CartItemEntity(
                            user.id(),
                            item.productId(),
                            item.quantity()
                    )
            );

            return saved.getId();
        }

        if (existing.isDeleted()) existing.active();

        existing.applyQuantity(item.quantity());

        return existing.getId();
    }

    @Transactional
    public Long modifyCartItem(User user, ModifyCartItem item) {
        CartItemEntity found = cartItemRepository.findByUserIdAndIdAndStatus(user.id(), item.cartItemId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        found.applyQuantity(item.quantity());

        return found.getId();
    }

    @Transactional
    public void deleteCartItem(User user, Long cartItemId) {
        CartItemEntity entity = cartItemRepository.findByUserIdAndIdAndStatus(user.id(), cartItemId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        entity.delete();
    }
}
