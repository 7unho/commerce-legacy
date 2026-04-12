package io.april2nd.commerce.core.support;

import java.util.List;

public record Page<T>(
        List<T> content,
        Boolean hasNext
) {}
