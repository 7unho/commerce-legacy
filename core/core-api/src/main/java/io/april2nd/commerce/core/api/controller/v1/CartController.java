package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.AddCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.request.CreateSharedCartRequest;
import io.april2nd.commerce.core.api.controller.v1.request.ModifyCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.response.AcceptSharedCartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.CartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.CreateSharedCartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.SharedCartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.SharedCartSummaryResponse;
import io.april2nd.commerce.core.domain.Cart;
import io.april2nd.commerce.core.domain.CartService;
import io.april2nd.commerce.core.domain.CreatedSharedCart;
import io.april2nd.commerce.core.domain.SharedCartService;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final SharedCartService sharedCartService;

    @GetMapping("/v1/cart")
    ApiResponse<CartResponse> getCart(User user) {
        Cart cart = cartService.getCart(user);
        return ApiResponse.success(
                new CartResponse(
                        cart.items()
                                .stream()
                                .map(CartResponse.CartItemResponse::of)
                                .collect(Collectors.toList())
                )
        );
    }

    @PostMapping("/v1/cart/items")
    ApiResponse<Void> addCartItem(
            User user,
            @RequestBody AddCartItemRequest request) {
        cartService.addCartItem(user, request.toAddCartItem());
        return ApiResponse.success();
    }

    @PutMapping("/v1/cart/items/{cartItemId}")
    ApiResponse<Void> modifyCartItem(
            User user,
            @PathVariable Long cartItemId,
            @RequestBody ModifyCartItemRequest request) {
        cartService.modifyCartItem(user, request.toModifyCartItem(cartItemId));
        return ApiResponse.success();
    }

    @DeleteMapping("/v1/cart/items/{cartItemId}")
    ApiResponse<Void> deleteCartItem(
            User user,
            @PathVariable Long cartItemId) {
        cartService.deleteCartItem(user, cartItemId);
        return ApiResponse.success();
    }

    @PostMapping("/v1/shared-carts")
    ApiResponse<CreateSharedCartResponse> createSharedCart(
            User user,
            @RequestBody CreateSharedCartRequest request) {
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

    @PostMapping("/v1/cart/{accessKey}/access")
    ApiResponse<AcceptSharedCartResponse> acceptSharedCart(
            User user,
            @PathVariable String accessKey) {
        return ApiResponse.success(new AcceptSharedCartResponse(
                sharedCartService.accept(user, accessKey)
        ));
    }

    @DeleteMapping("/v1/shared-carts/{cartId}")
    ApiResponse<Void> deleteSharedCart(User user, @PathVariable Long cartId) {
        sharedCartService.delete(user, cartId);
        return ApiResponse.success();
    }
}
