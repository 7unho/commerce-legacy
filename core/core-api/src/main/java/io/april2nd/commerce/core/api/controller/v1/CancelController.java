package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.CancelRequest;
import io.april2nd.commerce.core.domain.CancelService;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CancelController {
    private final CancelService cancelService;

    @PostMapping("/v1/cancel")
    ApiResponse<Void> cancel(User user, @RequestBody CancelRequest request) {
        cancelService.cancel(user, request.toCancelAction());
        return ApiResponse.success();
    }
}
