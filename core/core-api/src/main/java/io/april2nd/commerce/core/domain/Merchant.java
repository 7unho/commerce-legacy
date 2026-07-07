package io.april2nd.commerce.core.domain;

public record Merchant(
        Long id,
        String name,
        Long settlementCycle
) {
}
