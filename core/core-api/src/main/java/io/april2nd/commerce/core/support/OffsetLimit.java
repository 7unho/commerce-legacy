package io.april2nd.commerce.core.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record OffsetLimit(
        Integer offset,
        Integer limit
) {
    public Pageable toPageable() {
        return PageRequest.of(offset / limit, limit);
    }
}
