package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.AddCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.request.ModifyCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.response.CartResponse;
import io.april2nd.commerce.core.domain.Cart;
import io.april2nd.commerce.core.domain.CartService;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CartController {
    private CartService cartService;

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
}
