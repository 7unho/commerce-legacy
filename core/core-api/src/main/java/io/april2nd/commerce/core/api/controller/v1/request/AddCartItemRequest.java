package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.AddCartItem;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

public record AddCartItemRequest(
        Long productId,
        Long quantity
) {
    public AddCartItem toAddCartItem() {
        if (quantity <= 0) throw new CoreException(ErrorType.INVALID_REQUEST);

        return new AddCartItem(productId, quantity);
    }
}
