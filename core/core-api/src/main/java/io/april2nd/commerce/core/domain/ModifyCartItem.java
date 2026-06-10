package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

public record ModifyCartItem(
        Long cartItemId,
        Long quantity
) {
    public ModifyCartItem {
        if (cartItemId == null) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (quantity == null || quantity <= 0) throw new CoreException(ErrorType.INVALID_REQUEST);
    }
}
