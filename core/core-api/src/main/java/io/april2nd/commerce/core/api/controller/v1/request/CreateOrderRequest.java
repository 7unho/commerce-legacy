package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.NewOrder;
import io.april2nd.commerce.core.domain.NewOrderItem;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

import java.util.List;

public record CreateOrderRequest(
        List<CreateOrderTarget> targets
) {
    public NewOrder toNewOrder(User user) {
        if (targets.isEmpty()) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (targets.stream().anyMatch(it -> it.quantity <= 0)) throw new CoreException(ErrorType.INVALID_REQUEST);

        return new NewOrder(
                user.id(),
                targets.stream()
                        .map(it ->
                                new NewOrderItem(
                                        it.productId,
                                        it.productOptionId,
                                        it.quantity
                                )
                        )
                        .toList()
        );
    }

    public record CreateOrderTarget(
            Long productId,
            Long productOptionId,
            Long quantity
    ) {}
}
