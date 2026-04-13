package io.april2nd.commerce.core.domain;

public record Answer(
        Long id,
        Long adminId,
        String content
) {
    public static final Answer EMPTY = new Answer(-1L, -1L, "");
}
