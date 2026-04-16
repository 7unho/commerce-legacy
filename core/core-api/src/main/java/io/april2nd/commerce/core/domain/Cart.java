package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record Cart(
        Long userId,
        List<CartItem> items
) {
    public NewOrder toNewOrder(Set<Long> targetItemIds) {
        if (items.isEmpty()) throw new CoreException(ErrorType.INVALID_REQUEST);

        return new NewOrder(
                userId,
                items.stream()
                        .filter(it -> targetItemIds.contains(it.id()))
                        .map(it -> new NewOrderItem(
                                it.product().id(),
                                it.quantity()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
