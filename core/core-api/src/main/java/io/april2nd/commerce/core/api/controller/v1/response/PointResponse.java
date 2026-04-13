package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.PointBalance;
import io.april2nd.commerce.core.domain.PointHistory;
import io.april2nd.commerce.core.enums.PointType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PointResponse(
        Long userId,
        BigDecimal balance,
        List<PointHistoryResponse> histories
) {
    public static PointResponse of(PointBalance balance, List<PointHistory> histories) {
        return new PointResponse(
                balance.userId(),
                balance.balance(),
                histories.stream()
                        .map(history ->
                                new PointHistoryResponse(
                                        history.type(),
                                        history.amount(),
                                        history.appliedAt()
                                ))
                        .collect(Collectors.toList())
        );
    }

    private record PointHistoryResponse(
            PointType type,
            BigDecimal amount,
            LocalDateTime appliedAt
    ) {}
}

