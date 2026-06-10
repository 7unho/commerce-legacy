package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.AddCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.request.ModifyCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.response.CartResponse;
import io.april2nd.commerce.core.api.controller.v1.response.SharedCartResponse;
import io.april2nd.commerce.core.domain.Cart;
import io.april2nd.commerce.core.domain.CartService;
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
    ApiResponse<SharedCartResponse> createSharedCart(User user) {
        return ApiResponse.success(SharedCartResponse.of(cartService.createSharedCarts(user)));
    }

    @GetMapping("/v1/shared-carts")
    ApiResponse<List<SharedCartResponse>> getSharedCarts(User user) {
        return ApiResponse.success(SharedCartResponse.of(cartService.getAccessibleCart(user)));
    }

    @GetMapping("/v1/shared-cart/{cartId}")
    ApiResponse<CartResponse> getSharedCart(User user, @PathVariable Long cartId) {
        Cart cart = cartService.getSharedCart(user, cartId);
        return ApiResponse.success(
                new CartResponse(
                        cart.items().stream()
                                .map(CartResponse.CartItemResponse::of)
                                .toList()
                )
        );
    }

    @PostMapping("/v1/cart/{accessKey}/access")
    ApiResponse<Void> accessCart(
            User user,
            @PathVariable String accessKey) {
        cartService.access(user, accessKey);
        return ApiResponse.success();
    }

    @DeleteMapping("/v1/cart/{cartId}")
    ApiResponse<Void> deleteCart(User user, @PathVariable Long cartId) {
        cartService.deleteCart(user, cartId);
        return ApiResponse.success();
    }
}
