package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartEntity;
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
public class SharedCartManager {
    private static final long EXPIRATION_DAYS = 7L;

    private final CartRepository cartRepository;
    private final CartMemberRepository cartMemberRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CreatedSharedCart create(User user, CreateSharedCart command) {
        LocalDateTime now = LocalDateTime.now();
        CartEntity saved = cartRepository.save(new CartEntity(
                user.id(),
                CartType.SHARED,
                command.name().trim(),
                UUID.randomUUID().toString(),
                now.plusDays(EXPIRATION_DAYS)
        ));
        return new CreatedSharedCart(saved.getId(), saved.getShareToken(), saved.getExpiredAt());
    }

    @Transactional
    public Long accept(User user, String shareToken) {
        if (shareToken == null || shareToken.isBlank()) {
            throw new CoreException(ErrorType.CART_INVALID_SHARE_TOKEN);
        }

        CartEntity cart = cartRepository.findByShareTokenAndTypeAndStatus(
                        shareToken, CartType.SHARED, EntityStatus.ACTIVE
                )
                .orElseThrow(() -> new CoreException(ErrorType.CART_INVALID_SHARE_TOKEN));
        validateNotExpired(cart);

        if (cart.isOwner(user.id())) return cart.getId();

        LocalDateTime now = LocalDateTime.now();
        CartMemberEntity member = cartMemberRepository.findByCartIdAndUserId(cart.getId(), user.id())
                .orElse(null);
        if (member == null) {
            cartMemberRepository.save(new CartMemberEntity(cart.getId(), user.id(), now));
        } else if (member.isDeleted()) {
            member.accept(now);
        }
        return cart.getId();
    }

    @Transactional
    public void delete(User user, Long cartId) {
        CartEntity cart = cartRepository.findByIdAndTypeAndStatus(cartId, CartType.SHARED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        if (!cart.isOwner(user.id())) throw new CoreException(ErrorType.CART_ACCESS_DENIED);

        cart.delete();
        cartMemberRepository.findByCartIdAndStatus(cartId, EntityStatus.ACTIVE)
                .forEach(CartMemberEntity::delete);
        cartItemRepository.findByCartIdAndStatus(cartId, EntityStatus.ACTIVE)
                .forEach(it -> it.delete());
    }

    private void validateNotExpired(CartEntity cart) {
        if (cart.isExpired(LocalDateTime.now())) {
            throw new CoreException(ErrorType.CART_EXPIRED);
        }
    }
}
