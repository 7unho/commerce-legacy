package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartAccessEntity;
import io.april2nd.commerce.storage.db.core.CartAccessRepository;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import io.april2nd.commerce.storage.db.core.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartHandler {
    private final CartRepository cartRepository;
    private final CartAccessRepository cartAccessRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartAccess createSharedCart(Long userId) {
        CartEntity cart = cartRepository.save(new CartEntity(CartType.SHARED, userId));

        CartAccessEntity access = cartAccessRepository.save(
                new CartAccessEntity(
                        UUID.randomUUID().toString(), // NOTE: 중복이 불가능한 값으로 구성
                        cart.getId(),
                        cart.getType(),
                        cart.getUserId(),
                        cart.getUserId(),
                        CartPolicy.SHARED_EXPIRATION_DAYS.expireAt(LocalDateTime.now())
                )
        );

        return new CartAccess(
                access.getAccessKey(),
                access.getCartId(),
                access.getType(),
                access.getUserId(),
                access.getExpiredAt(),
                access.getCreatedAt(),
                access.getUpdatedAt()
        );
    }

    @Transactional
    public Long remove(Long userId, Long cartId) {
        CartEntity cart = cartRepository.findByIdAndUserIdAndStatus(cartId, userId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        if (cart.getType() == CartType.DEFAULT) throw new CoreException(ErrorType.CART_OPERATION_NOT_ALLOWED);

        cart.delete();
        cartAccessRepository.findByCartIdAndStatus(cartId, EntityStatus.ACTIVE)
                .forEach(CartAccessEntity::delete);
        cartItemRepository.findByCartIdAndStatus(cartId, EntityStatus.ACTIVE)
                .forEach(CartItemEntity::delete);
        return cart.getId();
    }

    @Transactional
    public void access(Long userId, String accessKey) {
        CartAccessEntity access = cartAccessRepository.findByAccessKeyAndStatus(accessKey, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        if (access.getType() == CartType.DEFAULT) throw new CoreException(ErrorType.CART_OPERATION_NOT_ALLOWED);
        if (access.isExpired()) throw new CoreException(ErrorType.CART_SHARED_EXPIRED);

        CartAccessEntity memberAccess = cartAccessRepository
                .findByCartIdAndAccessUserId(access.getCartId(), userId)
                .orElse(null);
        if (memberAccess == null) {
            cartAccessRepository.save(
                    new CartAccessEntity(
                            "%d-%d-%d".formatted(access.getCartId(), access.getUserId(), userId),
                            access.getCartId(),
                            access.getType(),
                            access.getUserId(),
                            userId,
                            access.getExpiredAt()
                    )
            );
        } else if (memberAccess.isDeleted()) {
            memberAccess.grant();
        }
    }
}
