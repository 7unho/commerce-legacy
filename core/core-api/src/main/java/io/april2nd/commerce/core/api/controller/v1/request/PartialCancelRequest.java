package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.PartialCancelAction;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

public record PartialCancelRequest(
        String orderKey,
        Long orderItemId,
        Long quantity
) {
    public PartialCancelAction toPartialCancelAction() {
        if (orderKey == null || orderKey.isBlank()) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (orderItemId == null) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (quantity == null || quantity <= 0) throw new CoreException(ErrorType.INVALID_REQUEST);

        return new PartialCancelAction(orderKey, orderItemId, quantity);
    }
}
