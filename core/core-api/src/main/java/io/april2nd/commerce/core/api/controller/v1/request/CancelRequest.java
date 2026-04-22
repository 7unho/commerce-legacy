package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.CancelAction;

public record CancelRequest(
        String orderKey
) {
    public CancelAction toCancelAction() {
        return new CancelAction(orderKey);
    }
}
