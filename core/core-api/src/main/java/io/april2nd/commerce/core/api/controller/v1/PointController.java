package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.response.PointResponse;
import io.april2nd.commerce.core.domain.PointBalance;
import io.april2nd.commerce.core.domain.PointHistory;
import io.april2nd.commerce.core.domain.PointService;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PointController {
    private PointService pointService;

    @GetMapping("/v1/point")
    ApiResponse<PointResponse> getPoint(User user) {
        PointBalance balance = pointService.balance(user);
        List<PointHistory> histories = pointService.histories(user);
        return ApiResponse.success(PointResponse.of(balance, histories));
    }
}
