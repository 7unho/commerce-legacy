package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CartManager {
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Long add(User user, AddCartItem item) {
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
    public Long modify(User user, ModifyCartItem item) {
        CartItemEntity found = cartItemRepository.findByUserIdAndIdAndStatus(user.id(), item.cartItemId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        found.applyQuantity(item.quantity());

        return found.getId();
    }

    @Transactional
    public void delete(User user, Long cartItemId) {
        CartItemEntity entity = cartItemRepository.findByUserIdAndIdAndStatus(user.id(), cartItemId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        entity.delete();
    }
}
