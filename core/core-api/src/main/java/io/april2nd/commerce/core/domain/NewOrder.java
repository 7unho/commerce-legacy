package io.april2nd.commerce.core.domain;

import java.util.List;

public record NewOrder(
        Long userId,
        List<NewOrderItem> items
) {}
