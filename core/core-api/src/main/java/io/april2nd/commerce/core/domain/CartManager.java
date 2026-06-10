package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import io.april2nd.commerce.storage.db.core.CartMemberEntity;
import io.april2nd.commerce.storage.db.core.CartMemberRepository;
import io.april2nd.commerce.storage.db.core.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
    public void deleteItem(User user, Long cartItemId) {
        CartItemEntity entity = cartItemRepository.findByIdAndStatus(cartItemId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        CartEntity cart = getActiveCart(entity.getCartId());
        validateOwner(user, cart);
        validateNotExpired(cart);
        entity.delete();
    }

    @Transactional
    public CreatedSharedCart createSharedCart(User user, CreateSharedCart command) {
        LocalDateTime now = LocalDateTime.now();
        CartEntity saved = cartRepository.save(new CartEntity(
                user.id(),
                CartType.SHARED,
                command.name().trim(),
                UUID.randomUUID().toString(),
                CartPolicy.SHARED_EXPIRATION_DAYS.expireAt(now)
        ));
        return new CreatedSharedCart(saved.getId(), saved.getShareToken(), saved.getExpiredAt());
    }

    @Transactional
    public Long acceptSharedCart(User user, String accessKey) {
        if (accessKey == null || accessKey.isBlank()) {
            throw new CoreException(ErrorType.CART_INVALID_SHARE_TOKEN);
        }

        CartEntity cart = cartRepository.findByShareTokenAndTypeAndStatus(
                        accessKey, CartType.SHARED, EntityStatus.ACTIVE
                )
                .orElseThrow(() -> new CoreException(ErrorType.CART_INVALID_SHARE_TOKEN));
        CartAccess ownerAccess = toOwnerAccess(cart);
        ownerAccess.validateNotExpired(LocalDateTime.now());

        if (ownerAccess.isAccessibleBy(user)) return ownerAccess.cartId();

        LocalDateTime now = LocalDateTime.now();
        CartMemberEntity member = cartMemberRepository.findByCartIdAndUserId(cart.getId(), user.id())
                .orElse(null);
        if (member == null) {
            member = cartMemberRepository.save(new CartMemberEntity(cart.getId(), user.id(), now));
        } else if (member.isDeleted()) {
            member.accept(now);
        }

        CartAccess memberAccess = toMemberAccess(cart, member);
        return memberAccess.cartId();
    }

    @Transactional
    public void deleteSharedCart(User user, Long cartId) {
        CartEntity cart = cartRepository.findByIdAndTypeAndStatus(cartId, CartType.SHARED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        CartAccess ownerAccess = toOwnerAccess(cart);
        ownerAccess.validateUser(user);

        cart.delete();
        cartMemberRepository.findByCartIdAndStatus(cartId, EntityStatus.ACTIVE)
                .forEach(CartMemberEntity::delete);
        cartItemRepository.findByCartIdAndStatus(cartId, EntityStatus.ACTIVE)
                .forEach(CartItemEntity::delete);
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
        CartAccess access = findAccess(user, cart);
        access.validate(user, LocalDateTime.now());
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
        toOwnerAccess(cart).validateNotExpired(LocalDateTime.now());
    }

    private CartAccess findAccess(User user, CartEntity cart) {
        if (cart.isOwner(user.id())) return toOwnerAccess(cart);

        return cartMemberRepository.findByCartIdAndUserId(cart.getId(), user.id())
                .filter(CartMemberEntity::isActive)
                .map(member -> toMemberAccess(cart, member))
                .orElseThrow(() -> new CoreException(ErrorType.CART_ACCESS_DENIED));
    }

    private CartAccess toOwnerAccess(CartEntity cart) {
        return new CartAccess(
                cart.getShareToken(), cart.getId(), cart.getType(), cart.getOwnerId(), cart.getExpiredAt(),
                cart.getCreatedAt(), cart.getUpdatedAt()
        );
    }

    private CartAccess toMemberAccess(CartEntity cart, CartMemberEntity member) {
        return new CartAccess(
                cart.getShareToken(), cart.getId(), cart.getType(), member.getUserId(), cart.getExpiredAt(),
                member.getCreatedAt(), member.getUpdatedAt()
        );
    }

}
