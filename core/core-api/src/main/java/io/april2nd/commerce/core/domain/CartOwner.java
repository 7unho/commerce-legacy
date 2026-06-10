package io.april2nd.commerce.core.domain;

public record CartOwner(
        Long cartId,
        Long cartOwnerId
) {
}