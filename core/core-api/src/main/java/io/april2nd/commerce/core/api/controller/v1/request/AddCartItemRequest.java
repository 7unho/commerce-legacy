package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.AddCartItem;

public record AddCartItemRequest(
        Long cartId,
        Long productId,
        Long quantity
) {
    public AddCartItemRequest(Long productId, Long quantity) {
        this(null, productId, quantity);
    }

    public AddCartItem toAddCartItem() {
        return new AddCartItem(cartId, productId, quantity);
    }
}
