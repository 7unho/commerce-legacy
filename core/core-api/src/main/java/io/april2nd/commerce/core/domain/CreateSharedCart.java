package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

public record CreateSharedCart(String name) {
    public CreateSharedCart {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.INVALID_REQUEST);
        }
    }
}
