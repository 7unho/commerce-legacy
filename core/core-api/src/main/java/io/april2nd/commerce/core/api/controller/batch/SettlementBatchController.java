package io.april2nd.commerce.core.api.controller.batch;

import io.april2nd.commerce.core.domain.SettlementService;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class SettlementBatchController {
    private SettlementService settlementService;

    /**
     * NOTE: 정산 대상 적재 배치
     * - 오전 1시 실행
     * - 어제 00:00:00 ~ 23:59:59 기준
     */
    @PostMapping("/internal-batch/load-targets")
    public ApiResponse<Void> loadTargets(
            @RequestParam(required = false) LocalDate targetDate
    ) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        settlementService.loadTargets(
                date,
                date.minusDays(1).atStartOfDay(),
                date.atStartOfDay().minusNanos(1)
        );

        return ApiResponse.success();
    }

    /**
     * NOTE: 정산 계산 배치
     * - 오전 4시 실행
     */
    @PostMapping("/internal-batch/calculate")
    public ApiResponse<Void> calculate(
            @RequestParam(required = false) LocalDate targetDate
    ) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        settlementService.calculate(date);

        return ApiResponse.success();
    }

    /**
     * NOTE: 정산 입금 배치
     * - 오전 9시 실행
     */
    @PostMapping("/internal-batch/transfer")
    public ApiResponse<Void> transfer() {
        settlementService.transfer();
        return ApiResponse.success();
    }
}