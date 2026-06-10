package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

import java.time.LocalDateTime;

public record CartAccess(
        String accessKey,
        Long cartId,
        CartType type,
        Long userId,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public boolean isAccessibleBy(User user) {
        return userId.equals(user.id());
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt != null && !now.isBefore(expiredAt);
    }

    public void validateUser(User user) {
        if (!isAccessibleBy(user)) throw new CoreException(ErrorType.CART_ACCESS_DENIED);
    }

    public void validateNotExpired(LocalDateTime now) {
        if (isExpired(now)) throw new CoreException(ErrorType.CART_EXPIRED);
    }

    public void validate(User user, LocalDateTime now) {
        validateUser(user);
        validateNotExpired(now);
    }
}
