package io.april2nd.commerce.core.domain;

public record OrderInvite(
        Long orderId,
        String inviteKey
) {}
