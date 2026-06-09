package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.AddCartItem;

public record AddCartItemRequest(
        Long productId,
        Long quantity
) {
    public AddCartItem toAddCartItem() {
        return new AddCartItem(productId, quantity);
    }
}
