package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import io.april2nd.commerce.storage.db.core.CartMemberRepository;
import io.april2nd.commerce.storage.db.core.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CartManager {
    private final CartRepository cartRepository;
    private final CartMemberRepository cartMemberRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Long add(User user, AddCartItem item) {
        CartEntity cart = item.cartId() == null
                ? getOrCreatePersonalCart(user)
                : getAccessibleSharedCart(user, item.cartId());
        validateNotExpired(cart);

        CartItemEntity existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), item.productId());

        if (existing == null) {
            CartItemEntity saved = cartItemRepository.save(
                    new CartItemEntity(
                            cart.getId(),
                            item.productId(),
                            item.quantity()
                    )
            );

            return saved.getId();
        }

        if (existing.isDeleted()) {
            existing.active();
            existing.applyQuantity(item.quantity());
        } else {
            existing.addQuantity(item.quantity());
        }

        return existing.getId();
    }

    @Transactional
    public Long modify(User user, ModifyCartItem item) {
        CartItemEntity found = cartItemRepository.findByIdAndStatus(item.cartItemId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        CartEntity cart = getActiveCart(found.getCartId());
        validateOwner(user, cart);
        validateNotExpired(cart);

        found.applyQuantity(item.quantity());

        return found.getId();
    }

    @Transactional
    public void delete(User user, Long cartItemId) {
        CartItemEntity entity = cartItemRepository.findByIdAndStatus(cartItemId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        CartEntity cart = getActiveCart(entity.getCartId());
        validateOwner(user, cart);
        validateNotExpired(cart);
        entity.delete();
    }

    private CartEntity getOrCreatePersonalCart(User user) {
        return cartRepository.findByOwnerIdAndTypeAndStatus(user.id(), CartType.PERSONAL, EntityStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(
                        new CartEntity(user.id(), CartType.PERSONAL, null, null, null)
                ));
    }

    private CartEntity getAccessibleSharedCart(User user, Long cartId) {
        CartEntity cart = cartRepository.findByIdAndTypeAndStatus(cartId, CartType.SHARED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        boolean accessible = cart.isOwner(user.id())
                || cartMemberRepository.existsByCartIdAndUserIdAndStatus(cartId, user.id(), EntityStatus.ACTIVE);
        if (!accessible) throw new CoreException(ErrorType.CART_ACCESS_DENIED);
        return cart;
    }

    private CartEntity getActiveCart(Long cartId) {
        return cartRepository.findById(cartId)
                .filter(CartEntity::isActive)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
    }

    private void validateOwner(User user, CartEntity cart) {
        if (!cart.isOwner(user.id())) throw new CoreException(ErrorType.CART_ACCESS_DENIED);
    }

    private void validateNotExpired(CartEntity cart) {
        if (cart.isExpired(java.time.LocalDateTime.now())) {
            throw new CoreException(ErrorType.CART_EXPIRED);
        }
    }
}
