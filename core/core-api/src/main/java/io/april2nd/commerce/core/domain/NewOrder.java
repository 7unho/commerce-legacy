package io.april2nd.commerce.core.domain;

import java.util.List;
import java.util.stream.Collectors;

public record NewOrder(
        Long userId,
        List<NewOrderItem> items
) {
    public List<Long> productOptionIds() {
        return items.stream()
                .map(NewOrderItem::productOptionId)
                .collect(Collectors.toList());
    }
}
