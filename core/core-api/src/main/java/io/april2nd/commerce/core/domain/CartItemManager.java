package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartItemManager {
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Long addItem(CartOwner owner, AddCartItem item) {
        CartItemEntity existing = cartItemRepository.findByCartIdAndProductIdAndProductOptionId(owner.cartId(), item.productId(), item.productOptionId());
        if (existing == null) {
            return cartItemRepository.save(
                    new CartItemEntity(
                            owner.cartOwnerId(),
                            owner.cartId(),
                            item.productId(),
                            item.productOptionId(),
                            item.quantity()
                    )
            ).getId();
        }

        if (existing.isDeleted()) existing.active();
        existing.applyQuantity(item.quantity());

        return existing.getId();
    }

    @Transactional
    public Long modifyItem(Long userId, ModifyCartItem item) {
        CartItemEntity found = cartItemRepository.findByUserIdAndIdAndStatus(userId, item.cartItemId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        found.applyQuantity(item.quantity());

        return found.getId();
    }

    @Transactional
    public void deleteItem(Long userId, Long cartItemId) {
        CartItemEntity found = cartItemRepository.findByUserIdAndIdAndStatus(userId, cartItemId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        found.delete();
    }

    @Transactional
    public void deleteItemsByProductOptions(Long userId, List<Long> productOptionIds) {
        cartItemRepository.findByUserIdAndProductOptionIdInAndStatus(userId, productOptionIds, EntityStatus.ACTIVE)
                .stream()
                .forEach(CartItemEntity::delete);
    }
}
