package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.AcceptSharedCartRequest;
import io.april2nd.commerce.core.api.controller.v1.request.CreateSharedCartRequest;
import io.april2nd.commerce.core.api.controller.v1.response.AcceptSharedCartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.CreateSharedCartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.SharedCartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.SharedCartSummaryResponse;
import io.april2nd.commerce.core.domain.CreatedSharedCart;
import io.april2nd.commerce.core.domain.SharedCartService;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SharedCartController {
    private final SharedCartService sharedCartService;

    @PostMapping("/v1/shared-carts")
    ApiResponse<CreateSharedCartResponse> create(User user, @RequestBody CreateSharedCartRequest request) {
        CreatedSharedCart cart = sharedCartService.create(user, request.toCreateSharedCart());
        return ApiResponse.success(CreateSharedCartResponse.of(cart));
    }

    @GetMapping("/v1/shared-carts")
    ApiResponse<List<SharedCartSummaryResponse>> getSharedCarts(User user) {
        return ApiResponse.success(SharedCartSummaryResponse.of(sharedCartService.getSharedCarts(user)));
    }

    @GetMapping("/v1/shared-carts/{cartId}")
    ApiResponse<SharedCartResponse> getSharedCart(User user, @PathVariable Long cartId) {
        return ApiResponse.success(SharedCartResponse.of(sharedCartService.getSharedCart(user, cartId)));
    }

    @PostMapping("/v1/shared-carts/accept")
    ApiResponse<AcceptSharedCartResponse> accept(User user, @RequestBody AcceptSharedCartRequest request) {
        return ApiResponse.success(new AcceptSharedCartResponse(
                sharedCartService.accept(user, request.shareToken())
        ));
    }

    @DeleteMapping("/v1/shared-carts/{cartId}")
    ApiResponse<Void> delete(User user, @PathVariable Long cartId) {
        sharedCartService.delete(user, cartId);
        return ApiResponse.success();
    }
}
