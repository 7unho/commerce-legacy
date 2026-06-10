package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

public record AddCartItem(
        Long cartId,
        Long productId,
        Long quantity
) {
    public AddCartItem(Long productId, Long quantity) {
        this(null, productId, quantity);
    }

    public AddCartItem {
        if (productId == null) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (quantity == null || quantity <= 0) throw new CoreException(ErrorType.INVALID_REQUEST);
    }
}
