package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartAccessEntity;
import io.april2nd.commerce.storage.db.core.CartAccessRepository;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CartVerifier {
    private final CartRepository cartRepository;
    private final CartAccessRepository cartAccessRepository;

    public CartOwner verifyAccess(Long userId, Long cartId) {
        CartEntity cart = cartRepository.findByIdAndStatus(cartId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        switch (cart.getType()) {
            case DEFAULT -> {
                if (!Objects.equals(cart.getUserId(), userId)) throw new CoreException(ErrorType.INVALID_REQUEST);
            }
            case SHARED -> {
                CartAccessEntity access = cartAccessRepository.findByCartIdAndAccessUserIdAndStatus(cart.getId(), userId, EntityStatus.ACTIVE)
                        .orElseThrow(() -> new CoreException(ErrorType.CART_SHARED_NOT_FOUND));
                if (access.isExpired()) throw new CoreException(ErrorType.CART_SHARED_EXPIRED);
            }
        }

        return new CartOwner(cart.getId(), cart.getUserId());
    }
}
