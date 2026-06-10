package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.CreateSharedCart;

public record CreateSharedCartRequest(String name) {
    public CreateSharedCart toCreateSharedCart() {
        return new CreateSharedCart(name);
    }
}
