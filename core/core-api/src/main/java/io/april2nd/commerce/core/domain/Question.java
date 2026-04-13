package io.april2nd.commerce.core.domain;

public record Question(
        Long id,
        Long userId,
        String title,
        String content
) {}
