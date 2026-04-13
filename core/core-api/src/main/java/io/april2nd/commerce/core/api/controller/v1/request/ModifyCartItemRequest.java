package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.ModifyCartItem;

public record ModifyCartItemRequest(
        Long quantity
) {
    public ModifyCartItem toModifyCartItem(Long cartItemId) {
        return new ModifyCartItem(cartItemId, quantity);
    }
}
