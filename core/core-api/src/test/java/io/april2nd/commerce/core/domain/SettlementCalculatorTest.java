package io.april2nd.commerce.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ContextConfiguration(classes = SettlementCalculator.class)
class SettlementCalculatorTest {
    private final SettlementCalculator settlementCalculator;

    SettlementCalculatorTest(@Autowired SettlementCalculator settlementCalculator) {
        this.settlementCalculator = settlementCalculator;
    }

    @Test
    @DisplayName("최근 매출이 1천만 이하이면 기본 정산 수수료 10%를 적용한다")
    void appliesDefaultFeeRate() {
        SettlementAmount amount = settlementCalculator.calculate(
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(10_000_000)
        );

        assertThat(amount.feeRate()).isEqualByComparingTo(BigDecimal.valueOf(0.1));
        assertThat(amount.feeAmount()).isEqualByComparingTo(BigDecimal.valueOf(10_000).setScale(2));
        assertThat(amount.settlementAmount()).isEqualByComparingTo(BigDecimal.valueOf(90_000).setScale(2));
    }

    @Test
    @DisplayName("최근 매출이 1천만 초과이면 정산 수수료 8%를 적용한다")
    void appliesTenMillionFeeRate() {
        SettlementAmount amount = settlementCalculator.calculate(
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(10_000_001)
        );

        assertThat(amount.feeRate()).isEqualByComparingTo(BigDecimal.valueOf(0.08));
        assertThat(amount.feeAmount()).isEqualByComparingTo(BigDecimal.valueOf(8_000).setScale(2));
        assertThat(amount.settlementAmount()).isEqualByComparingTo(BigDecimal.valueOf(92_000).setScale(2));
    }

    @Test
    @DisplayName("최근 매출이 정확히 1억이면 정산 수수료 8%를 적용한다")
    void appliesTenMillionFeeRateAtHundredMillionBoundary() {
        SettlementAmount amount = settlementCalculator.calculate(
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(100_000_000)
        );

        assertThat(amount.feeRate()).isEqualByComparingTo(BigDecimal.valueOf(0.08));
        assertThat(amount.feeAmount()).isEqualByComparingTo(BigDecimal.valueOf(8_000).setScale(2));
        assertThat(amount.settlementAmount()).isEqualByComparingTo(BigDecimal.valueOf(92_000).setScale(2));
    }

    @Test
    @DisplayName("최근 매출이 1억 초과이면 정산 수수료 5%를 적용한다")
    void appliesHundredMillionFeeRate() {
        SettlementAmount amount = settlementCalculator.calculate(
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(100_000_001)
        );

        assertThat(amount.feeRate()).isEqualByComparingTo(BigDecimal.valueOf(0.05));
        assertThat(amount.feeAmount()).isEqualByComparingTo(BigDecimal.valueOf(5_000).setScale(2));
        assertThat(amount.settlementAmount()).isEqualByComparingTo(BigDecimal.valueOf(95_000).setScale(2));
    }
}
