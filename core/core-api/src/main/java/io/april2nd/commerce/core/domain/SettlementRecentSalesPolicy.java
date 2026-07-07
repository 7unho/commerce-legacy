package io.april2nd.commerce.core.domain;

import java.time.LocalDate;

public enum SettlementRecentSalesPolicy {
    RANGE(1, 1);

    private final long startMonthsBefore;
    private final long endDaysBefore;

    SettlementRecentSalesPolicy(long startMonthsBefore, long endDaysBefore) {
        this.startMonthsBefore = startMonthsBefore;
        this.endDaysBefore = endDaysBefore;
    }

    public LocalDate startDate(LocalDate settlementDate) {
        return settlementDate.minusMonths(startMonthsBefore);
    }

    public LocalDate endDate(LocalDate settlementDate) {
        return settlementDate.minusDays(endDaysBefore);
    }
}
